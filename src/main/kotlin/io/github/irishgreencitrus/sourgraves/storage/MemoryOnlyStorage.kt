package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

/*
    A testing form of storage that only exists in memory once the server is up
 */
class MemoryOnlyStorage : GraveStorage() {
    private var graves: HashMap<UUID, GraveData> = hashMapOf()

    override operator fun contains(uuid: UUID): Boolean {
        return uuid in graves
    }
    override fun count(): Int = graves.count()
    override fun query(uuid: UUID): GraveData? = graves[uuid]
    override fun query(): HashMap<UUID, GraveData> = graves

    override fun write(uuid: UUID, data: GraveData) {
        graves[uuid] = data
    }

    override fun write(data: Map<UUID, GraveData>) {
        graves = HashMap(data)
    }

    override fun delete(uuid: UUID): GraveData? {
        return graves.remove(uuid)
    }

    override fun resetGraveTimers() {
        // Unimplemented as this storage is not persistent over a restart
    }

    override fun cleanupHardExpiredGraves() {
    }

    override fun sync() {
        // Unimplemented as this storage is not persistent over a restart
    }
}