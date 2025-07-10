package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

abstract class GraveStorage {
    operator fun get(uuid: UUID): GraveData? {
        return query(uuid)
    }

    operator fun set(uuid: UUID, data: GraveData) {
        write(uuid, data)
    }

    abstract operator fun contains(uuid: UUID): Boolean

    abstract fun count(): Int
    abstract fun query(uuid: UUID): GraveData?
    abstract fun query(): Map<UUID, GraveData>
    abstract fun write(uuid: UUID, data: GraveData)
    abstract fun write(data: Map<UUID, GraveData>)
    abstract fun delete(uuid: UUID): GraveData?
    abstract fun resetGraveTimers()
    abstract fun cleanupHardExpiredGraves()
    abstract fun sync()
}