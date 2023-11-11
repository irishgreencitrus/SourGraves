package io.github.irishgreencitrus.sourgraves

import org.bukkit.OfflinePlayer
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

class GraveHandler {
    private var graves: HashMap<OfflinePlayer, HashMap<UUID, GraveData>> = HashMap()
    fun addGrave(p: OfflinePlayer, graveId: UUID, graveData: GraveData) {
        val playerGraves = graves[p]
        if (playerGraves == null) {
            graves[p] = hashMapOf(Pair(graveId, graveData))
        } else {
            playerGraves[graveId] = graveData
        }
    }
    fun removeGrave(p: OfflinePlayer, graveId: UUID) : GraveData? {
        return graves[p]?.remove(graveId)
    }
}