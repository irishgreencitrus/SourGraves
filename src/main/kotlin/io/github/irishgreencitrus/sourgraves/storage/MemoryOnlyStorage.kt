package io.github.irishgreencitrus.sourgraves.storage

/*
    A testing form of storage that only exists in memory once the server is up
 */
class MemoryOnlyStorage : MemoryCachedStorage() {
    /*
        This is not applicable for memory-only storage
     */
    override fun sync() = Unit
}