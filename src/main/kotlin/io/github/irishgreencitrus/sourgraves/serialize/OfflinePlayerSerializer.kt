package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

object OfflinePlayerSerializer : KSerializer<OfflinePlayer> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OfflinePlayer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OfflinePlayer {
        val uuid = UUIDSerializer.deserialize(decoder)
        return Bukkit.getOfflinePlayer(uuid)
    }

    override fun serialize(encoder: Encoder, value: OfflinePlayer) {
        val uuid = value.uniqueId
        UUIDSerializer.serialize(encoder, uuid)
    }

}