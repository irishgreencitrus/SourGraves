package io.github.irishgreencitrus.sourgraves.persist

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

class GravePersistSqlite : GravePersist {
    override fun loadGraves(): HashMap<UUID, GraveData> {
        TODO("Not yet implemented")
    }

    override fun saveGraves(graves: HashMap<UUID, GraveData>): Boolean {
        TODO("Not yet implemented")
    }

    override fun persistenceValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun forceInitialize() {
        TODO("Not yet implemented")
    }
}