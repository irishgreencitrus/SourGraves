package io.github.irishgreencitrus.sourgraves.persist

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

interface GravePersist {
    fun loadGraves(): HashMap<UUID, GraveData>
    fun saveGraves(graves: HashMap<UUID, GraveData>): Boolean

    fun persistenceValid(): Boolean

    fun forceInitialize()

    fun initialize() {
        if (!persistenceValid()) {
            forceInitialize()
        }
    }
}