package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand

object ArmourStandSerializer : KSerializer<ArmorStand?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ArmorStand", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ArmorStand? {
        val uuid = UUIDSerializer.deserialize(decoder)
        return Bukkit.getEntity(uuid) as ArmorStand?
    }

    override fun serialize(encoder: Encoder, value: ArmorStand?) {
        val uuid = value!!.uniqueId
        UUIDSerializer.serialize(encoder, uuid)
    }

}