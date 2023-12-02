package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemStack
import java.util.*

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemStack", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ItemStack {
        val s = decoder.decodeString()
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(s))
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val bytes = value.serializeAsBytes()
        val base64Bytes = Base64.getEncoder().encode(bytes)
        encoder.encodeString(String(base64Bytes))
    }
}