package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.io.File
import java.time.Instant
import java.util.*

class GraveHandler {
    private val module = SerializersModule {
        contextual(UUID::class, UUIDSerializer)
    }

    @Serializable
    var graves: HashMap<UUID, GraveData> = HashMap()

    var gravesToRemove: HashMap<UUID, Pair<Int, Int>> = HashMap()

    var graveWithInvalidCache = hashSetOf<UUID>()

    fun writeGravesFile(dataFolder: File) {
        val graveFile = File(dataFolder, "graves.json")
        graveFile.parentFile.mkdirs()
        val serInstance = Json {
            explicitNulls = true
            serializersModule = module
        }
        graveFile.writeText(serInstance.encodeToString(graves))
    }

    fun loadGravesFile(dataFolder: File) {
        val graveFile = File(dataFolder, "graves.json")
        if (!graveFile.exists()) return
        val serInstance = Json {
            explicitNulls = true
            serializersModule = module
        }
        graves = serInstance.decodeFromString(graveFile.readText())
        SourGraves.plugin.logger.info("Loaded ${graves.count()} grave(s)")
    }

    fun resetGraveTimers() {
        graves.forEach {
            it.value.createdAt = Instant.now()
        }
    }

    operator fun set(graveId: UUID, graveData: GraveData) {
        graves[graveId] = graveData
    }

    fun removeGrave(graveId: UUID) : GraveData? {
        return graves.remove(graveId)
    }

    fun findOwnedGraves(player: OfflinePlayer) : Map<UUID,GraveData> {
        return graves.filterValues { graveData ->
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
        if (!graves.containsKey(graveId)) return false
        val grave = graves[graveId]!!
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
        val grave = graves[uuid] ?: return null
        val stand = GraveHelper.getArmourStandEntity(SourGraves.plugin.server, uuid)

        if (stand != null) {
            graveWithInvalidCache.remove(uuid)
            grave.cachedLocation = stand.location
        }

        if (grave.cachedLocation.world == null) return null
        return Pair(grave.cachedLocation, grave)
    }

    private fun purgeGrave(uuid: UUID) {
        val armourUuid = graves[uuid]!!.linkedArmourStandUuid
        val armourStand = SourGraves.plugin.server.getEntity(armourUuid)
        armourStand?.remove()
        removeGrave(uuid)
    }

    fun purgeGraveDropItems(uuid: UUID, tooManyGraves: Boolean = false, chunkLoadEvent: Boolean = false) {
        if (!chunkLoadEvent && gravesToRemove.containsKey(uuid)) return

        val grave = graves[uuid] ?: return
        val loc = locateGrave(uuid)?.first
        if (loc == null) {
            graveWithInvalidCache.add(uuid)
            return
        }

        if (loc.world == null) {
            SourGraves.plugin.logger.warning("Location is invalid, it should")
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

    fun cleanupHardExpiredGraves() {
        val iter = graves.iterator()
        while (iter.hasNext()) {
            val (u, g) = iter.next()
            if (GraveHelper.isGraveQueuedForDeletion(g)) {
                purgeGraveDropItems(u)
            }
        }
    }


    operator fun contains(uuid: UUID) : Boolean {
        return graves.containsKey(uuid)
    }

    operator fun get(uuid: UUID) : GraveData? {
        return graves[uuid]
    }
}