package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

class MySqlStorage : GraveStorage() {
    override fun contains(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override fun count(): Int {
        TODO("Not yet implemented")
    }

    override fun query(uuid: UUID): GraveData? {
        TODO("Not yet implemented")
    }

    override fun query(): Map<UUID, GraveData> {
        TODO("Not yet implemented")
    }

    override fun write(uuid: UUID, data: GraveData) {
        TODO("Not yet implemented")
    }

    override fun write(data: Map<UUID, GraveData>) {
        TODO("Not yet implemented")
    }

    override fun delete(uuid: UUID): GraveData? {
        TODO("Not yet implemented")
    }

    override fun resetGraveTimers() {
        TODO("Not yet implemented")
    }

    override fun cleanupHardExpiredGraves() {
        TODO("Not yet implemented")
    }

    override fun sync() {
        TODO("Not yet implemented")
    }
}
