package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

class MemoryOnlyStorage : GraveStorage() {
    var graves: HashMap<UUID, GraveData> = hashMapOf()

    override fun count(): Int = graves.count()
    override fun query(uuid: UUID): GraveData? = graves[uuid]
    override fun query(): HashMap<UUID, GraveData> = graves

    override fun write(uuid: UUID, data: GraveData) {
        graves[uuid] = data
    }

    override fun write(data: HashMap<UUID, GraveData>) {
        graves = data
    }

    override fun delete(uuid: UUID) {
        graves.remove(uuid)
    }
}