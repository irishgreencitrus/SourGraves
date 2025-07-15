package io.github.irishgreencitrus.sourgraves

import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*

class GraveInventory(val graveUuid: UUID, name: Component) : InventoryHolder {
    private val inv: Inventory = SourGraves.plugin.server.createInventory(this, 9 * 5, name)
    override fun getInventory() = inv
}