package io.github.irishgreencitrus.sourgraves

import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import java.util.*

class SourGraveListener : Listener {
    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val inv = e.player.inventory
        if (inv.isEmpty) return

        val graveId = UUID.randomUUID()
        e.drops.clear()
        val armourStand = e.player.world.spawnEntity(e.player.location.subtract(0.0,1.3,0.0), EntityType.ARMOR_STAND) as ArmorStand
        GraveHelper.makeGraveArmourStand(armourStand, graveId, e.player, message = e.deathMessage() ?: Component.text("${e.player.name} died"))
        SourGraves.plugin.graveHandler.addGrave(
            e.player,
            graveId,
            inv.contents.toList())
    }
    @EventHandler
    fun onPlayerInteractAtEntity(e: PlayerInteractAtEntityEvent) {
        if (e.rightClicked.type != EntityType.ARMOR_STAND) return
        val armourStand: ArmorStand = e.rightClicked as ArmorStand
        if (armourStand.hasMetadata("sour_grave_owner")) {
            val ownerUUID = UUID.fromString(armourStand.getMetadata("sour_grave_owner")[0].asString())
            if (ownerUUID == e.player.uniqueId) {
                armourStand.world.spawnParticle(Particle.SOUL, armourStand.location, 100)
                armourStand.remove()
                val graveUUID = UUID.fromString(armourStand.getMetadata("sour_grave_id")[0].asString())
                e.player.inventory.contents =
                    SourGraves.plugin.graveHandler.removeGrave(e.player, graveUUID)?.toTypedArray()!!
            }
        }
    }
}