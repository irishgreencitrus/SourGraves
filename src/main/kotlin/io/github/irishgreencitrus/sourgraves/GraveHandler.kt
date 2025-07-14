package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.SourGraves.Companion.plugin
import io.github.irishgreencitrus.sourgraves.SourGraves.Companion.storage
import org.bukkit.Location
import java.util.*

class GraveHandler {
    var gravesToRemove: HashMap<UUID, Pair<Int, Int>> = HashMap()

    var graveWithInvalidCache = hashSetOf<UUID>()

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
            if (plugin.pluginConfig.logMessages.armourStandNotFoundOnGravePurge)
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