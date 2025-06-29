package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location

object LocationSerializer : KSerializer<Location> {
    private val delegateSerializer = MapSerializer(String.serializer(), String.serializer())
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("org.bukkit.Location", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Location {
        val map = decoder.decodeSerializableValue(delegateSerializer)
        val loc = Location.deserialize(map)
        return loc
    }

    override fun serialize(encoder: Encoder, value: Location) {
        val map = value.serialize().mapValues { it.value.toString() }
        encoder.encodeSerializableValue(delegateSerializer, map)
    }
}