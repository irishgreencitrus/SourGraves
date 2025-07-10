package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.serialize.InstantSerializer
import io.github.irishgreencitrus.sourgraves.serialize.ItemStackSerializer
import io.github.irishgreencitrus.sourgraves.serialize.LocationSerializer
import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Location
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
    @Serializable(with = InstantSerializer::class)
    var timerStartedAt: Instant = Instant.now(),
    @Serializable(with = UUIDSerializer::class)
    val linkedArmourStandUuid: UUID,
    @Serializable(with = LocationSerializer::class)

    // This prevents the plugin from breaking when updating.
    // In reality this should never be accessed directly because it could be wrong.
    // In a new server environment, this should never be wrong.
    var cachedLocation: Location = Location(null, 0.0, 0.0, 0.0)
)