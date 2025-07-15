package io.github.irishgreencitrus.sourgraves.storage

import io.github.irishgreencitrus.sourgraves.GraveData
import io.github.irishgreencitrus.sourgraves.GraveHelper
import io.github.irishgreencitrus.sourgraves.SourGraves
import org.bukkit.inventory.ItemStack
import java.util.*

/*
    Storage that uses an in-memory hashmap to do its primary computation, and
    may write to disk on sync()
 */
abstract class MemoryCachedStorage : GraveStorage() {
    protected var graves: HashMap<UUID, GraveData> = hashMapOf()
    fun queryAll() = graves

    override operator fun contains(uuid: UUID): Boolean {
        return uuid in graves
    }

    override fun count(): Int = graves.count()
    override fun query(uuid: UUID): GraveData? = graves[uuid]

    override fun write(uuid: UUID, data: GraveData) {
        graves[uuid] = data
    }

    override fun updateItems(uuid: UUID, items: List<ItemStack?>) {
        graves[uuid]?.items = items
    }

    override fun delete(uuid: UUID) {
        graves.remove(uuid)
    }

    override fun cleanupHardExpiredGraves() {
        val mutIter = graves.iterator()
        while (mutIter.hasNext()) {
            val (u, g) = mutIter.next()
            if (GraveHelper.isGraveQueuedForDeletion(g)) {
                SourGraves.plugin.graveHandler.purgeGraveDropItems(u)
            }
        }
    }

    override fun newestGrave(playerUUID: UUID, dimension: String?): Pair<UUID, GraveData>? {
        return searchPlayerGraves(playerUUID, dimension).minByOrNull { it.value.createdAt }?.toPair()
    }

    override fun oldestGrave(playerUUID: UUID, dimension: String?): Pair<UUID, GraveData>? {
        return searchPlayerGraves(playerUUID, dimension).maxByOrNull { it.value.createdAt }?.toPair()
    }

    override fun searchPlayerGraves(playerUUID: UUID, dimension: String?): Map<UUID, GraveData> {
        return if (dimension == null) {
            graves.filterValues { graveData ->
                graveData.ownerUuid == playerUUID
            }
        } else {
            graves.filterValues { graveData ->
                graveData.ownerUuid == playerUUID && graveData.cachedLocation.world?.name == dimension
            }
        }
    }
}