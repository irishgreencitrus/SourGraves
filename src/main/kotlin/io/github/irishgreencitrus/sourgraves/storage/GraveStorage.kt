package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import org.bukkit.OfflinePlayer
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

    // There is no way to query the entire database at once anymore.
    // This is by design.
    abstract fun query(uuid: UUID): GraveData?

    abstract fun write(uuid: UUID, data: GraveData)
    abstract fun write(data: Map<UUID, GraveData>)
    abstract fun delete(uuid: UUID): GraveData?

    /**
     * Get a list of all the graves that belong to a given player.
     * This should be implemented in the most efficient way for the given storage.
     *
     * @param playerUUID The player to search the graves for.
     * @param dimension The dimension to restrict your search to. If null, include all dimensions.
     *
     * @return A map of all the graves belonging to the player. If empty, no graves belong to this player.
     */
    abstract fun searchPlayerGraves(playerUUID: UUID, dimension: String? = null): Map<UUID, GraveData>
    fun searchPlayerGraves(player: OfflinePlayer, dimension: String? = null): Map<UUID, GraveData> =
        searchPlayerGraves(player.uniqueId, dimension)

    /**
     * Get the oldest grave belonging to a given player.
     *
     * @param playerUUID The player to search for.
     * @param dimension The dimension to restrict your search to. If null, include all dimensions.
     *
     * @return The grave in question, or null if there are no graves belonging to that player.
     */
    abstract fun oldestGrave(playerUUID: UUID, dimension: String? = null): Pair<UUID, GraveData>?
    fun oldestGrave(player: OfflinePlayer, dimension: String? = null): Pair<UUID, GraveData>? =
        oldestGrave(player.uniqueId, dimension)

    /**
     * Get the newest grave belonging to a given player.
     *
     * @param playerUUID The player to search for.
     * @param dimension The dimension to restrict your search to. If null, include all dimensions.
     *
     * @return The grave in question, or null if there are no graves belonging to that player.
     */
    abstract fun newestGrave(playerUUID: UUID, dimension: String? = null): Pair<UUID, GraveData>?
    fun newestGrave(player: OfflinePlayer, dimension: String? = null): Pair<UUID, GraveData>? =
        newestGrave(player.uniqueId, dimension)

    /**
     *  This should reset all the grave timers to `Instant.now()`.
     *  This prevents them from being deleted on a restart and gives a buffer zone.
     */
    abstract fun resetGraveTimers()

    /*
        The cleanup task has been moved to here.
        This is where all the invalid graves should actually be removed.
     */
    abstract fun cleanupHardExpiredGraves()

    /*
        Any pending transactions should always be committed to disk here.
        Think of it as flushing the buffer.
        More resilient storages may want to call directly out to their storage in `write()` etc.
        That would make this unimplemented.
     */
    abstract fun sync()
}