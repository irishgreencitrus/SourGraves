package io.github.irishgreencitrus.sourgraves.storage

abstract class SQLStorage : GraveStorage() {
    // We don't need to sync, as SQL works directly with the database at all times.
    override fun sync() = Unit

    /**
     * Add all the entries from a `MemoryCachedStorage` into an `SQLStorage`.
     *
     * This function is designed to be called once for one set of data, and could error if called multiple times.
     */
    open fun convertFrom(oldStorage: MemoryCachedStorage) {
        for ((uuid, data) in oldStorage.queryAll()) {
            if (contains(uuid)) continue
            write(uuid, data)
        }
    }
}