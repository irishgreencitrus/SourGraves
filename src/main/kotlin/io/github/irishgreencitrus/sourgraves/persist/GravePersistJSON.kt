package io.github.irishgreencitrus.sourgraves.persist

import io.github.irishgreencitrus.sourgraves.GraveData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class GravePersistJSON(private val saveFile: File) : GravePersist {
    override fun loadGraves(): HashMap<UUID, GraveData> {
        val text = saveFile.readText()
        return Json.decodeFromString(text)
    }

    override fun saveGraves(graves: HashMap<UUID, GraveData>): Boolean {
        val out = Json.encodeToString(graves)
        saveFile.writeText(out)
        return true
    }

    override fun persistenceValid(): Boolean {
        return saveFile.exists()
    }

    override fun forceInitialize() {
        saveFile.mkdirs()
        saveGraves(HashMap())
    }
}