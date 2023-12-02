@file:UseSerializers(UUIDSerializer::class)
package io.github.irishgreencitrus.sourgraves.persist

import io.github.irishgreencitrus.sourgraves.GraveData
import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class GravePersistJSON(private val saveFile: File) : GravePersist {
    override fun loadGraves(): HashMap<@Serializable(with = UUIDSerializer::class) UUID, GraveData> {
        val text = saveFile.readText()
        return Json.decodeFromString(text)
    }

    override fun saveGraves(graves: HashMap<@Serializable(with = UUIDSerializer::class) UUID, GraveData>): Boolean {
        val out = Json.encodeToString(graves)
        Json.encodeToString(graves)
        saveFile.writeText(out)
        return true
    }

    override fun persistenceValid(): Boolean {
        return saveFile.exists()
    }

    override fun forceInitialize() {
        saveFile.parentFile.mkdirs()
        saveGraves(HashMap())
    }
}