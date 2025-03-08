package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.serialize.InstantSerializer
import io.github.irishgreencitrus.sourgraves.serialize.ItemStackSerializer
import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.*

@Serializable
data class GraveData(
    @Serializable(with = UUIDSerializer::class)
    val ownerUuid: UUID,
    val items: List<
            @Serializable(with = ItemStackSerializer::class)
            ItemStack?>,
    @Serializable(with = InstantSerializer::class)
    var createdAt: Instant,
    @Serializable(with = UUIDSerializer::class)
    val linkedArmourStandUuid: UUID
)