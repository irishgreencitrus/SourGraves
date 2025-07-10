package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import java.util.*

class PostgresStorage : GraveStorage() {
    override fun count(): Int {
        TODO("Not yet implemented")
    }

    override fun query(uuid: UUID): GraveData {
        TODO("Not yet implemented")
    }

    override fun query(): HashMap<UUID, GraveData> {
        TODO("Not yet implemented")
    }

    override fun write(uuid: UUID, data: GraveData) {
        TODO("Not yet implemented")
    }

    override fun write(data: HashMap<UUID, GraveData>) {
        TODO("Not yet implemented")
    }

    override fun delete(uuid: UUID) {
        TODO("Not yet implemented")
    }
}