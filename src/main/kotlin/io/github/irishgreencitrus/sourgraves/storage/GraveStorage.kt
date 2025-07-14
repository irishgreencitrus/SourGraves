package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import org.bukkit.OfflinePlayer
import java.util.*

abstract class GraveStorage {
    /**
     * Alias for `query(UUID): GraveData?`
     */
    operator fun get(uuid: UUID): GraveData? {
        return query(uuid)
    }

    /**
     * Alias for `write(UUID, GraveData)`
     */
    operator fun set(uuid: UUID, data: GraveData) {
        write(uuid, data)
    }

    /**
     * Call this before using the storage.
     * The inheritor is responsible for deciding whether to create the storage or load it.
     *
     * @return Whether the storage was successfully initialised.
     */
    abstract fun init(): Boolean

    abstract operator fun contains(uuid: UUID): Boolean

    abstract fun count(): Int

    // There is no way to query the entire database at once anymore.
    // This is by design.
    abstract fun query(uuid: UUID): GraveData?

    abstract fun write(uuid: UUID, data: GraveData)
    abstract fun delete(uuid: UUID)

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
     * Remove all the graves that have expired.
     *
     * In some storages this will delete them permanently, in others they will be marked as inaccessible
     */
    abstract fun cleanupHardExpiredGraves()

    /**
     * After this function is called, all data should be persisted to the disk.
     *
     * It is acceptable for this function to do nothing, in which case the data is written to the persistent
     * storage on every other call.
     */
    abstract fun sync()
}