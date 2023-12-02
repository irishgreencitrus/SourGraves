package io.github.irishgreencitrus.sourgraves

import org.bukkit.OfflinePlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime

//@Serializable(with = )
data class GraveData(
    val owner: OfflinePlayer,
    val items: List<ItemStack?>,
    val createdAt: LocalDateTime,
    val expireInMinutes: Int,
    val hardExpireInMinutes: Int,
    val linkedArmourStand: ArmorStand
) {
    fun hasExpired(): Boolean {
        val expiryDate = createdAt.plusMinutes(expireInMinutes.toLong())
        return expiryDate.isBefore(LocalDateTime.now())
    }
    fun hasHardExpired(): Boolean {
        val expiryDate = createdAt.plusMinutes(hardExpireInMinutes.toLong())
        return expiryDate.isBefore(LocalDateTime.now())
    }
}
