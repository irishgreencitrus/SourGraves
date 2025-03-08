package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.serialize.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.bukkit.OfflinePlayer
import java.io.File
import java.time.LocalDateTime
import java.util.*

class GraveHandler {

    private val module = SerializersModule {
        contextual(UUID::class, UUIDSerializer)
    }

    @Serializable
    private var graves: HashMap<UUID, GraveData> = HashMap()

    fun writeGravesFile(dataFolder: File) {
        val graveFile = File(dataFolder, "graves.json")
        if (!graveFile.exists()) {
            graveFile.parentFile.mkdirs()
            val serInstance = Json {
                explicitNulls = true
                serializersModule = module
            }
            graveFile.writeText(serInstance.encodeToString(graves))
        }
    }

    fun loadGravesFile(dataFolder: File) {
        val graveFile = File(dataFolder, "graves.json")
        if (!graveFile.exists()) return
        val serInstance = Json {
            explicitNulls = true
            serializersModule = module
        }
        graves = serInstance.decodeFromString(graveFile.readText())
    }

    fun resetGraveTimers() {
        graves.forEach {
            it.value.createdAt = LocalDateTime.now()
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

    fun findOldestGrave(player: OfflinePlayer) : Pair<UUID, GraveData>? {
        return findOwnedGraves(player).minByOrNull {
            it.value.createdAt
        }?.toPair()
    }

    private fun purgeGrave(uuid: UUID) {
        val armourUuid = graves[uuid]!!.linkedArmourStandUuid
        val armourStand = SourGraves.plugin.server.getEntity(armourUuid)
        armourStand?.remove()
        removeGrave(uuid)
    }

    fun purgeGraveDropItems(uuid: UUID) {
        val grave = graves[uuid] ?: return
        val armourUuid = graves[uuid]!!.linkedArmourStandUuid
        val armourStand = SourGraves.plugin.server.getEntity(armourUuid)
        val armourStandLocation = armourStand!!.location
        grave.items.filterNotNull().forEach {
            armourStand.world.dropItemNaturally(armourStandLocation, it)
        }
        purgeGrave(uuid)
    }

    fun cleanupHardExpiredGraves() {
        graves.forEach { (u, g) ->
            if (g.isGraveQueuedForDeletion())
                purgeGraveDropItems(u)
        }
    }

    operator fun contains(uuid: UUID) : Boolean {
        return graves.containsKey(uuid)
    }

    operator fun get(uuid: UUID) : GraveData? {
        return graves[uuid]
    }
}