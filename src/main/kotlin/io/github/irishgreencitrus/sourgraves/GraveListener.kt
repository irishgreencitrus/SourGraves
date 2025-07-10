package io.github.irishgreencitrus.sourgraves

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import io.github.irishgreencitrus.sourgraves.config.PaymentType
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class GraveListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val handl = SourGraves.plugin.graveHandler
        val stor = SourGraves.storage
        val cfg = SourGraves.plugin.pluginConfig
        val inv = e.player.inventory
        if (inv.isEmpty) return
        if (e.keepInventory) return

        val graveId = UUID.randomUUID()
        e.drops.clear()

        val armourStand = e.player.world.spawnEntity(e.player.location.subtract(0.0,1.3,0.0), EntityType.ARMOR_STAND) as ArmorStand
        GraveHelper.makeGraveArmourStand(armourStand, graveId, e.player, message = e.deathMessage() ?: Component.text("${e.player.name} died"))
        val currentGraves = stor.searchPlayerGraves(e.player)
        if (cfg.maxGravesPerPlayer != -1) {
            if (currentGraves.size >= cfg.maxGravesPerPlayer) {
                val oldestGrave = stor.oldestGrave(e.player)!!
                handl.purgeGraveDropItems(oldestGrave.first, tooManyGraves = true)
            }
        }
        stor[graveId] = GraveData(
            items = inv.contents.toList(),
            createdAt = Instant.now(),
            ownerUuid = e.player.uniqueId,
            linkedArmourStandUuid = armourStand.uniqueId,
            cachedLocation = e.player.location
        )

    }

    @EventHandler
    fun onPlayerInteractAtEntity(e: PlayerInteractAtEntityEvent) {
        if (e.rightClicked.type != EntityType.ARMOR_STAND) return
        val cfg = SourGraves.plugin.pluginConfig

        val armourStand: ArmorStand = e.rightClicked as ArmorStand
        val key = NamespacedKey(SourGraves.plugin, "sour_grave_id")
        if (!armourStand.persistentDataContainer.has(key)) return

        val graveUUID = UUID.fromString(armourStand.persistentDataContainer.get(key, PersistentDataType.STRING))
        val grave = SourGraves.storage[graveUUID] ?: return

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

            val balance = SourGraves.economy?.balance(SourGraves.plugin.name, e.player.uniqueId) ?: BigDecimal.ZERO
            val graveSize = SourGraves.storage[graveUUID]!!.items.size
            val multiplier = if (cfg.economy.graveRecoverPaymentType == PaymentType.FLAT) 1 else graveSize
            if (playerIsOwner) {
                val recoverCost = BigDecimal.valueOf(cfg.economy.graveRecoverCost * multiplier)
                if (balance >= recoverCost) {
                    val response =
                        SourGraves.economy?.withdraw(SourGraves.plugin.name, e.player.uniqueId, recoverCost)!!
                    if (!response.transactionSuccess()) return
                    e.player.sendMessage(
                        Component.text(
                            "Successfully recovered the grave for " + SourGraves.economy?.format(
                                SourGraves.plugin.name,
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
                val robCost = BigDecimal.valueOf(cfg.economy.graveRobCost * multiplier)
                if (balance >= BigDecimal.valueOf(cfg.economy.graveRobCost)) {
                    val response = SourGraves.economy?.withdraw(SourGraves.plugin.name, e.player.uniqueId, robCost)!!
                    if (!response.transactionSuccess()) return
                    e.player.sendMessage(
                        Component.text(
                            "Successfully robbed the grave for " + SourGraves.economy?.format(
                                SourGraves.plugin.name,
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

        armourStand.world.spawnParticle(Particle.valueOf(cfg.recoverParticle), armourStand.location.add(0.0,2.0,0.0), cfg.recoverParticleAmount)

        e.player.playSound(
            Sound.sound(
                Key.key(cfg.recoverSound), Sound.Source.PLAYER, 1f, 1f))
        armourStand.remove()

        val oldContents = e.player.inventory.contents.clone().filterNotNull()
        val graveValue = SourGraves.storage.delete(graveUUID)!!
        e.player.inventory.contents = graveValue.items.toTypedArray()

        val leftOvers = e.player.inventory.addItem(*oldContents.toTypedArray())
        leftOvers.values.forEach {
            e.player.world.dropItemNaturally(e.player.location,it)
        }

    }

    @EventHandler
    fun onChunkLoad(e: ChunkLoadEvent) {
        if (e.isNewChunk) return
        val toRemove = SourGraves.plugin.graveHandler.gravesToRemove
        val coord = Pair(e.chunk.x, e.chunk.z)
        if (!toRemove.containsValue(coord)) return

        // If we've loaded a chunk with a grave with an invalid cache, update the cached location
        val iter = SourGraves.plugin.graveHandler.graveWithInvalidCache.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            val grave = SourGraves.storage[it]!!
            val entity = e.world.getEntity(it)
            if (entity != null) {
                grave.cachedLocation = entity.location
                iter.remove()
            }
        }

        toRemove.filterValues { it == coord }.forEach {
            SourGraves.plugin.graveHandler.purgeGraveDropItems(it.key, chunkLoadEvent = true)
        }
    }

    @EventHandler
    fun onPlayerRespawn(e: PlayerPostRespawnEvent) {
        val cfg = SourGraves.plugin.pluginConfig

        if (!cfg.notifyCoordsOnRespawn) return
        val grave = SourGraves.storage.newestGrave(e.player) ?: return
        val locatedGrave = SourGraves.plugin.graveHandler.locateGrave(grave.first) ?: return
        val gravePos = locatedGrave.first

        e.player.sendMessage(
            Component.text("Your most recent grave is at ${gravePos.blockX}, ${gravePos.blockY}, ${gravePos.blockZ} in ${gravePos.world.name}")
                .color(NamedTextColor.YELLOW)
        )

    }
}