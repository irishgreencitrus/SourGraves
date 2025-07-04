package io.github.irishgreencitrus.sourgraves

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.time.Instant
import java.util.*

object GraveHelper {
    fun makeGraveArmourStand(armourStand: ArmorStand, graveId: UUID, player: OfflinePlayer, message: Component) {
        val playerSkull = ItemStack(Material.PLAYER_HEAD)
        val skullMeta = (playerSkull.itemMeta as SkullMeta?)!!
        skullMeta.setOwningPlayer(player)
        playerSkull.itemMeta = skullMeta
        with(armourStand) {
            setItem(EquipmentSlot.HEAD, playerSkull)
            setGravity(false)
            isInvisible = true
            isInvulnerable = true
            persistentDataContainer.set(
                NamespacedKey(SourGraves.plugin, "sour_grave_id"),
                PersistentDataType.STRING,
                graveId.toString()
            )
            customName(message.color(NamedTextColor.RED))
            isCustomNameVisible = true
            setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        }
    }

    fun isGravePublic(graveData: GraveData): Boolean {
        val cfg = SourGraves.plugin.pluginConfig
        if (cfg.publicInMinutes == -1) return false
        val expiryDate = graveData.createdAt.plusSeconds(cfg.publicInMinutes.toLong() * 60)
        return expiryDate.isBefore(Instant.now())
    }

    fun isGraveQueuedForDeletion(graveData: GraveData): Boolean {
        val cfg = SourGraves.plugin.pluginConfig
        if (cfg.deleteInMinutes == -1) return false
        val deletionDateTime = graveData.createdAt.plusSeconds(cfg.deleteInMinutes.toLong() * 60)
        return deletionDateTime.isBefore(Instant.now())
    }

    fun getArmourStandEntity(server: Server, uuid: UUID): Entity? {
        val entity = server.getEntity(uuid)
        return entity
    }

    fun getArmourStandEntity(server: Server, graveData: GraveData): Entity? {
        val entity = server.getEntity(graveData.linkedArmourStandUuid)
        return entity
    }

    fun getArmourStandLocation(server: Server, graveData: GraveData): Location? {
        val loc = getArmourStandEntity(server, graveData)?.location
        if (loc != null)
            graveData.cachedLocation = loc
        return getArmourStandEntity(server, graveData)?.location ?: (
                if (graveData.cachedLocation.world != null)
                    graveData.cachedLocation
                else
                    null
                )
    }
}