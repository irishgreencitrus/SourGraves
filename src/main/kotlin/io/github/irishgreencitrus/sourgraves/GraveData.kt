package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.serialize.ItemStackSerializer
import io.github.irishgreencitrus.sourgraves.serialize.LocalDateTimeSerializer
import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class GraveData(
    @Serializable(with = UUIDSerializer::class)
    val ownerUuid: UUID,
    val items: List<
            @Serializable(with = ItemStackSerializer::class)
            ItemStack?>,
    @Serializable(with = LocalDateTimeSerializer::class)
    var createdAt: LocalDateTime,
    val publicInMinutes: Int,
    val deletedInMinutes: Int,
    @Serializable(with = UUIDSerializer::class)
    val linkedArmourStandUuid: UUID
) {
    fun isGravePublic(): Boolean {
        val expiryDate = createdAt.plusMinutes(publicInMinutes.toLong())
        return expiryDate.isBefore(LocalDateTime.now())
    }

    fun isGraveQueuedForDeletion(): Boolean {
        val expiryDate = createdAt.plusMinutes(deletedInMinutes.toLong())
        return expiryDate.isBefore(LocalDateTime.now())
    }
}