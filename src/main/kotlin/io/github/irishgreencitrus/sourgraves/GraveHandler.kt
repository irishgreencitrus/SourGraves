package io.github.irishgreencitrus.sourgraves

import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

class GraveHandler {
    private val storage = SourGraves.storage

    var gravesToRemove: HashMap<UUID, Pair<Int, Int>> = HashMap()

    var graveWithInvalidCache = hashSetOf<UUID>()

    fun findOwnedGraves(player: OfflinePlayer) : Map<UUID,GraveData> {
        return storage.query().filterValues { graveData ->
            graveData.ownerUuid == player.uniqueId
        }
    }

    fun findSameDimensionGraves(player: Player): Map<UUID, GraveData> {
        return findOwnedGraves(player).filterValues { graveData ->
            val armourStand = player.server.getEntity(graveData.linkedArmourStandUuid)
            armourStand != null && armourStand.world.uid == player.world.uid
        }
    }

    fun graveInPlayerDimension(player: Player, graveId: UUID): Boolean {
        if (graveId !in storage) return false
        val grave = storage[graveId]!!
        val armourStand = player.server.getEntity(grave.linkedArmourStandUuid)
        return armourStand != null && armourStand.world.uid == player.world.uid
    }

    fun playerSameDimensionGravesByAge(player: Player): List<Pair<UUID, GraveData>> {
        return findSameDimensionGraves(player).toList().sortedBy {
            it.second.createdAt
        }
    }

    fun findOldestGrave(player: OfflinePlayer) : Pair<UUID, GraveData>? {
        return findOwnedGraves(player).minByOrNull {
            it.value.createdAt
        }?.toPair()
    }

    fun findNewestGrave(player: OfflinePlayer): Pair<UUID, GraveData>? {
        return findOwnedGraves(player).maxByOrNull {
            it.value.createdAt
        }?.toPair()
    }

    fun locateGrave(uuid: UUID): Pair<Location, GraveData>? {
        val grave = storage[uuid] ?: return null
        val stand = GraveHelper.getArmourStandEntity(SourGraves.plugin.server, uuid)

        if (stand != null) {
            graveWithInvalidCache.remove(uuid)
            grave.cachedLocation = stand.location
        }

        if (grave.cachedLocation.world == null) return null
        return Pair(grave.cachedLocation, grave)
    }

    private fun purgeGrave(uuid: UUID) {
        val armourUuid = storage[uuid]!!.linkedArmourStandUuid
        val armourStand = SourGraves.plugin.server.getEntity(armourUuid)
        armourStand?.remove()
        storage.delete(uuid)
    }

    private var messagePrinted = false
    fun purgeGraveDropItems(uuid: UUID, tooManyGraves: Boolean = false, chunkLoadEvent: Boolean = false) {
        if (!chunkLoadEvent && gravesToRemove.containsKey(uuid)) return

        val grave = storage[uuid] ?: return
        val loc = locateGrave(uuid)?.first
        if (loc == null) {
            graveWithInvalidCache.add(uuid)
            return
        }

        if (loc.world == null) {
            if (!messagePrinted)
                SourGraves.plugin.logger.warning("Armour stand location is invalid, this normally means you have upgraded from a version before 1.5.0.\nThis message will only be printed once.")
            messagePrinted = true
            return
        }

        if (!loc.isChunkLoaded) {
            gravesToRemove[uuid] = Pair(loc.chunk.x, loc.chunk.z)
            return
        }

        if (gravesToRemove.containsKey(uuid))
            gravesToRemove.remove(uuid)

        val armourUuid = grave.linkedArmourStandUuid
        val armourStand = SourGraves.plugin.server.getEntity(armourUuid)

        if (armourStand == null) {
            SourGraves.plugin.logger.warning("Armour stand not found with uuid $uuid, but chunk is loaded. Perhaps it has been killed?")
            return
        }

        val cfg = SourGraves.plugin.pluginConfig

        if ((tooManyGraves && cfg.dropItemsOnTooManyGraves) || cfg.dropItemsOnGraveDeletion) {
            val armourStandLocation = armourStand.location
            grave.items.filterNotNull().forEach {
                armourStand.world.dropItemNaturally(armourStandLocation, it)
            }
        }

        purgeGrave(uuid)
    }
}