package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemStack", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ItemStack {
        val s = decoder.decodeString()
        return ItemStack.deserializeBytes(Base64Coder.decode(s))
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val bytes = value.serializeAsBytes()
        val base64Bytes = Base64Coder.encode(bytes).toString()
        encoder.encodeString(base64Bytes)
    }
}