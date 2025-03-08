package io.github.irishgreencitrus.sourgraves.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemStack

object ItemStackSerializer : KSerializer<ItemStack> {
    private val delegateSerializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("org.bukkit.inventory.ItemStack", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): ItemStack {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        val item = ItemStack.deserializeBytes(array)
        return item
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val items = value.serializeAsBytes()
        encoder.encodeSerializableValue(delegateSerializer, items)
    }
}