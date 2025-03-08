package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("java.time.LocalDateTime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val epochLong = decoder.decodeLong()
        val time = Instant.ofEpochMilli(epochLong).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return time
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val epoch = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        encoder.encodeLong(epoch)
    }
}