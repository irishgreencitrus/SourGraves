package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

abstract class GraveStorage {
    abstract fun count(): Int
    abstract fun query(uuid: UUID): GraveData?
    abstract fun query(): HashMap<UUID, GraveData>
    abstract fun write(uuid: UUID, data: GraveData)
    abstract fun write(data: HashMap<UUID, GraveData>)
    abstract fun delete(uuid: UUID)
}