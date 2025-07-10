package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import java.util.*

/*
    The traditional SourGraves storage.
    This is also known as `graves.json`.

    This is the **most likely** persistent storage to break just by bad luck.
    Not much I can do about that given if the plugin / server crashes horribly data will most likely be lost.

    If you have a large server, or very critical data (i.e. more than an SMP with a few people in)
    I'd suggest using SQL based storage instead.
 */
class FileBackedStorage(private val dataFolder: File) : MemoryCachedStorage() {
    private val module = SerializersModule {
        contextual(UUID::class, UUIDSerializer)
    }

    private val serInstance = Json {
        explicitNulls = true
        serializersModule = module
    }

    init {
        val graveFile = File(dataFolder, "graves.json")
        if (graveFile.exists()) {
            graves = serInstance.decodeFromString(graveFile.readText())
        }
    }

    // We assume that whatever is in memory is the ground-truth.
    // If you don't like this assumption,
    // you should choose another type of storage.
    override fun sync() {
        val graveFile = File(dataFolder, "graves.json")
        graveFile.parentFile.mkdirs()
        graveFile.writeText(serInstance.encodeToString(graves))
    }
}