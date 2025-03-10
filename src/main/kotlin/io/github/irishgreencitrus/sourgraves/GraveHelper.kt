package io.github.irishgreencitrus.sourgraves

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.entity.ArmorStand
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
        val expiryDate = graveData.createdAt.plusSeconds(cfg.publicInMinutes.toLong() * 60)
        return expiryDate.isBefore(Instant.now())
    }

    fun isGraveQueuedForDeletion(graveData: GraveData): Boolean {
        val cfg = SourGraves.plugin.pluginConfig
        val deletionDateTime = graveData.createdAt.plusSeconds(cfg.deleteInMinutes.toLong() * 60)
        return deletionDateTime.isBefore(Instant.now())
    }
}