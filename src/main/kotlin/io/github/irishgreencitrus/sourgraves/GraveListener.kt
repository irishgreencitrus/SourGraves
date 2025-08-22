package io.github.irishgreencitrus.sourgraves

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import io.github.irishgreencitrus.sourgraves.SourGraves.Companion.plugin
import io.github.irishgreencitrus.sourgraves.SourGraves.Companion.storage
import io.github.irishgreencitrus.sourgraves.config.PaymentType
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class GraveListener : Listener {

    // Allow other plugins to modify the inventory before we store it in a grave.
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val handl = plugin.graveHandler
        val stor = storage
        val cfg = plugin.pluginConfig
        val inv = e.player.inventory
        if (e.player.world.name in cfg.disabledWorlds) return
        if (e.isCancelled) return
        if (cfg.disableForPvpKills) {
            if (e.player.killer != null) return
            @Suppress("UnstableApiUsage")
            if (e.damageSource.causingEntity is Player) return
        }
        if (inv.isEmpty) return

        val graveId = UUID.randomUUID()

        val armourStand = e.player.world.spawnEntity(e.player.location.subtract(0.0,1.3,0.0), EntityType.ARMOR_STAND) as ArmorStand
        GraveHelper.makeGraveArmourStand(armourStand, graveId, e.player, message = e.deathMessage() ?: Component.text("${e.player.name} died"))
        val currentGraves = stor.searchPlayerGraves(e.player)
        if (cfg.maxGravesPerPlayer != -1) {
            if (currentGraves.size >= cfg.maxGravesPerPlayer) {
                val oldestGrave = stor.oldestGrave(e.player)!!
                handl.deleteGraveFromWorld(oldestGrave.first, tooManyGraves = true)
            }
        }

        val player = e.player

        // NOTE: We read the inventory rather than drops, which could cause an issue with other plugins.
        //       File an issue if this is the case!
        val graveData = GraveData(
            items = player.inventory.contents.toList().filterNot {
                if (it != null) {
                    shouldDeleteItem(it)
                } else {
                    false
                }
            },
            createdAt = Instant.now(),
            timerStartedAtGameTime = e.player.world.gameTime,
            ownerUuid = e.player.uniqueId,
            linkedArmourStandUuid = armourStand.uniqueId,
            cachedLocation = e.player.location
        )

        plugin.storage[graveId] = graveData
        e.drops.clear()
    }

    private fun shouldDeleteItem(item: ItemStack): Boolean {
        return item.containsEnchantment(Enchantment.VANISHING_CURSE)
    }

    @EventHandler
    fun onPlayerInteractAtEntity(e: PlayerInteractAtEntityEvent) {
        if (e.rightClicked.type != EntityType.ARMOR_STAND) return
        val shiftClick = e.player.isSneaking

        val cfg = plugin.pluginConfig

        val armourStand: ArmorStand = e.rightClicked as ArmorStand
        val key = NamespacedKey(plugin, "sour_grave_id")
        if (!armourStand.persistentDataContainer.has(key)) return

        val graveUUID = UUID.fromString(armourStand.persistentDataContainer.get(key, PersistentDataType.STRING))
        val grave = storage[graveUUID] ?: return

        val playerIsOwner = e.player.uniqueId == grave.ownerUuid
        val canAccess =
            playerIsOwner || GraveHelper.isGravePublic(grave) || e.player.hasPermission("sourgraves.player.graveaccess")

        if (!canAccess) {
            e.player.sendMessage(Component.text("You can't access this grave").color(NamedTextColor.YELLOW))
            return
        }

        if (cfg.economy.enable) {
            // FIXME(maybe)
            //  we are assuming that a player's account UUID is the same as their UUID.
            //  I *think* this is normally the case, but it's not always guaranteed.
            //  If people start reporting bugs with economy have a look here.

            val balance = SourGraves.economy?.balance(plugin.name, e.player.uniqueId) ?: BigDecimal.ZERO
            val graveSize = storage[graveUUID]!!.items.size
            if (playerIsOwner) {
                val multiplier = if (cfg.economy.graveRecoverPaymentType == PaymentType.FLAT) 1 else graveSize
                val recoverCost = BigDecimal.valueOf(cfg.economy.graveRecoverCost * multiplier)
                if (balance >= recoverCost) {
                    val response =
                        SourGraves.economy?.withdraw(plugin.name, e.player.uniqueId, recoverCost)!!
                    if (!response.transactionSuccess()) return
                    e.player.sendMessage(
                        Component.text(
                            "Successfully recovered the grave for " + SourGraves.economy?.format(
                                plugin.name,
                                recoverCost
                            )
                        ).color(NamedTextColor.GREEN)
                    )
                } else {
                    e.player.sendMessage(
                        Component.text("You cannot afford to perform this action!").color(NamedTextColor.RED)
                    )
                    return
                }
            } else {
                val multiplier = if (cfg.economy.graveRobPaymentType == PaymentType.FLAT) 1 else graveSize
                val robCost = BigDecimal.valueOf(cfg.economy.graveRobCost * multiplier)
                if (balance >= BigDecimal.valueOf(cfg.economy.graveRobCost)) {
                    val response = SourGraves.economy?.withdraw(plugin.name, e.player.uniqueId, robCost)!!
                    if (!response.transactionSuccess()) return
                    e.player.sendMessage(
                        Component.text(
                            "Successfully robbed the grave for " + SourGraves.economy?.format(
                                plugin.name,
                                robCost
                            )
                        ).color(NamedTextColor.GREEN)
                    )
                } else {
                    e.player.sendMessage(
                        Component.text("You cannot afford to perform this action!").color(NamedTextColor.RED)
                    )
                    return
                }
            }
        }

        if (cfg.allowChestLikeGraveAccess) {
            if (shiftClick) restoreGrave(e.player, graveUUID, armourStand)
            else openGraveInventory(e.player, graveUUID)
        } else {
            restoreGrave(e.player, graveUUID, armourStand)
        }
    }

    private fun restoreGrave(player: Player, graveUUID: UUID, armourStand: ArmorStand) {
        val cfg = plugin.pluginConfig

        armourStand.world.spawnParticle(Particle.valueOf(cfg.recoverParticle), armourStand.location.add(0.0,2.0,0.0), cfg.recoverParticleAmount)

        player.playSound(
            Sound.sound(
                Key.key(cfg.recoverSound), Sound.Source.PLAYER, 1f, 1f))
        armourStand.remove()

        // Clone the inventory, so we don't overwrite it
        val leftoverContents = player.inventory.contents.clone()

        val data = storage[graveUUID]
        if (data == null) {
            plugin.logger.warning("Trying to restore grave with UUID ${graveUUID}, but it seems to not exist in the database?")
            return
        }

        storage.delete(graveUUID)

        for ((i, item) in data.items.withIndex()) {
            // If we don't have an item in the grave at that given slot, leave the inventory be.
            if (item == null) {
                leftoverContents[i] = null
            } else {
                player.inventory.setItem(i, item)
            }
        }

        val toBeDropped = player.inventory.addItem(*leftoverContents.filterNotNull().toTypedArray())
        toBeDropped.values.forEach {
            player.world.dropItemNaturally(player.location, it)
        }
    }

    private fun openGraveInventory(player: Player, graveUUID: UUID) {
        if (graveUUID !in storage) return
        val name = player.displayName().append(Component.text("'s Grave"))
        val inv = GraveInventory(graveUUID, name)
        player.openInventory(inv.inventory)
    }

    @EventHandler
    fun onChunkLoad(e: ChunkLoadEvent) {
        if (e.isNewChunk) return
        val toRemove = plugin.graveHandler.gravesToRemove
        val coord = Pair(e.chunk.x, e.chunk.z)
        if (!toRemove.containsValue(coord)) return

        // If we've loaded a chunk with a grave with an invalid cache, update the cached location
        val iter = plugin.graveHandler.graveWithInvalidCache.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            val grave = storage[it]!!
            val entity = e.world.getEntity(it)
            if (entity != null) {
                grave.cachedLocation = entity.location
                iter.remove()
            }
        }

        toRemove.filterValues { it == coord }.forEach {
            plugin.graveHandler.deleteGraveFromWorld(it.key, chunkLoadEvent = true)
        }
    }

    @EventHandler
    fun onPlayerRespawn(e: PlayerPostRespawnEvent) {
        val cfg = plugin.pluginConfig

        if (!cfg.notifyCoordsOnRespawn) return
        val grave = storage.newestGrave(e.player) ?: return
        val locatedGrave = plugin.graveHandler.locateGrave(grave.first) ?: return
        val gravePos = locatedGrave.first

        e.player.sendMessage(
            Component.text("Your most recent grave is at ${gravePos.blockX}, ${gravePos.blockY}, ${gravePos.blockZ} in ${gravePos.world.name}")
                .color(NamedTextColor.YELLOW)
        )
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory.holder == null) return
        if (event.inventory.holder !is GraveInventory) return
        val holder = event.inventory.holder as GraveInventory
        if (event.inventory != holder.inventory) return
        storage.query(holder.graveUuid)?.items?.forEachIndexed { i, item ->
            holder.inventory.setItem(i, item)
        }
        (41..<45).forEach {
            val item = ItemStack.of(Material.BARRIER)
            val meta = item.itemMeta
            meta.displayName(Component.text("Not Accessible").color(NamedTextColor.RED))
            item.itemMeta = meta
            holder.inventory.setItem(it, item)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder == null) return
        if (event.inventory.holder !is GraveInventory) return
        val holder = event.inventory.holder as GraveInventory
        if (event.inventory != holder.inventory) return

        if (event.currentItem?.type == Material.BARRIER) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder == null) return
        if (event.inventory.holder !is GraveInventory) return
        val holder = event.inventory.holder as GraveInventory

        // Slots above 41 are inaccessible, and do not map to a real inventory slot.
        val relevantContents = holder.inventory.contents.slice(0..<41)

        // There's not much point updating the stored items if we're going to purge the grave anyway
        if (relevantContents.all { it == null }) {
            plugin.graveHandler.deleteGraveFromWorld(holder.graveUuid, canDropItems = false)
            return
        }
        storage.updateItems(holder.graveUuid, relevantContents.toList())
    }
}