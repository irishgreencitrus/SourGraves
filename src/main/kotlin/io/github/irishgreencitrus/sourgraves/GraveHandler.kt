package io.github.irishgreencitrus.sourgraves

import org.bukkit.OfflinePlayer
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

class GraveHandler {
    var graves: HashMap<OfflinePlayer, HashMap<UUID, List<ItemStack?>>> = HashMap()
    fun addGrave(p: OfflinePlayer, graveId: UUID, items: List<ItemStack?>) {
        val playerGraves = graves[p]
        if (playerGraves == null) {
            graves[p] = hashMapOf(Pair(graveId, items))
        } else {
            playerGraves[graveId] = items
        }
    }
    fun removeGrave(p: OfflinePlayer, graveId: UUID) : List<ItemStack?>? {
        return graves[p]?.remove(graveId)
    }
}