package io.github.irishgreencitrus.sourgraves

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.persistence.PersistentDataType
import java.time.Instant
import java.util.*

class GraveListener : Listener {
    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val handl = SourGraves.plugin.graveHandler
        val cfg = SourGraves.plugin.pluginConfig
        val inv = e.player.inventory
        if (inv.isEmpty) return
        val graveId = UUID.randomUUID()
        e.drops.clear()

        val armourStand = e.player.world.spawnEntity(e.player.location.subtract(0.0,1.3,0.0), EntityType.ARMOR_STAND) as ArmorStand
        GraveHelper.makeGraveArmourStand(armourStand, graveId, e.player, message = e.deathMessage() ?: Component.text("${e.player.name} died"))
        val currentGraves = handl.findOwnedGraves(e.player)
        if (currentGraves.size >= cfg.maxGravesPerPlayer) {
            val oldestGrave = handl.findOldestGrave(e.player)!!
            handl.purgeGraveDropItems(oldestGrave.first)
        }
        handl[graveId] = GraveData(
            items = inv.contents.toList(),
            createdAt = Instant.now(),
            ownerUuid = e.player.uniqueId,
            linkedArmourStandUuid = armourStand.uniqueId
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
        val grave = SourGraves.plugin.graveHandler[graveUUID] ?: return
        val canAccess = (e.player.uniqueId == grave.ownerUuid) || GraveHelper.isGravePublic(grave)

        if (!canAccess) {
            e.player.sendMessage(Component.text("You can't access this grave").color(NamedTextColor.YELLOW))
        }

        armourStand.world.spawnParticle(Particle.valueOf(cfg.recoverParticle), armourStand.location.add(0.0,2.0,0.0), cfg.recoverParticleAmount)

        e.player.playSound(
            Sound.sound(
                Key.key(cfg.recoverSound), Sound.Source.PLAYER, 1f, 1f))
        armourStand.remove()

        val oldContents = e.player.inventory.contents.clone().filterNotNull()
        val graveValue = SourGraves.plugin.graveHandler.removeGrave(graveUUID)!!
        e.player.inventory.contents = graveValue.items.toTypedArray()

        val leftOvers = e.player.inventory.addItem(*oldContents.toTypedArray())
        leftOvers.values.forEach {
            e.player.world.dropItemNaturally(e.player.location,it)
        }

    }
}