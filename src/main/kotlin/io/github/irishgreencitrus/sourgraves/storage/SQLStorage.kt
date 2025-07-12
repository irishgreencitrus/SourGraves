package io.github.irishgreencitrus.sourgraves.storage

abstract class SQLStorage : GraveStorage() {
    // We don't need to sync, as SQL works directly with the database at all times.
    override fun sync() = Unit
    abstract fun convertFrom(oldStorage: MemoryCachedStorage)
}