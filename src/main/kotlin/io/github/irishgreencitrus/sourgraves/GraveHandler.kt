package io.github.irishgreencitrus.sourgraves

import org.bukkit.OfflinePlayer
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

class GraveHandler {
    private var maxGraves: Int = 3;

    private var graves: HashMap<UUID, GraveData> = HashMap()
    private var hardExpiries: HashMap<UUID, LocalDateTime> = HashMap()
    operator fun set(graveId: UUID, graveData: GraveData) {
        graves[graveId] = graveData
        hardExpiries[graveId] =
            graveData.createdAt.plusMinutes(graveData.expireInMinutes.toLong())
    }
    fun removeGrave(graveId: UUID) : GraveData? {
        return graves.remove(graveId)
    }
    fun findOwnedGraves(player: OfflinePlayer) : Map<UUID,GraveData> {
        return graves.filterValues { graveData ->
            graveData.owner == player
        }
    }
    fun hasGraveExpired(uuid: UUID) : Boolean? {
        val time = graves[uuid]?.createdAt ?: return null
        val expiredTime = time.plusMinutes(10L)
        return expiredTime.isBefore(LocalDateTime.now())
    }

    private fun purgeGrave(uuid: UUID) {
        graves[uuid]!!.linkedArmourStand.remove();
        removeGrave(uuid);
    }
    private fun purgeGraveDropItems(uuid: UUID) {
        val grave = graves[uuid] ?: return
        val armourStandLocation = grave.linkedArmourStand.location
        grave.items.filterNotNull().forEach {
            grave.linkedArmourStand.world.dropItemNaturally(armourStandLocation, it)
        }
        purgeGrave(uuid)
    }

    fun cleanupHardExpiredGraves() {
        hardExpiries.forEach{ (u, t) ->
            if (t.isAfter(LocalDateTime.now())) purgeGraveDropItems(u)
        }
    }

    operator fun contains(uuid: UUID) : Boolean {
        return graves.containsKey(uuid)
    }

    operator fun get(uuid: UUID) : GraveData? {
        return graves[uuid]
    }
}