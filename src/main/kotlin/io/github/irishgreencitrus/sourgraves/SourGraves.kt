package io.github.irishgreencitrus.sourgraves

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SourGraves : JavaPlugin() {
    companion object {
        val plugin: SourGraves get() {
            return getPlugin(SourGraves::class.java)
        }
    }
    var graveHandler = GraveHandler()
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(SourGraveListener(), this)
        logger.info("irishgreencitrus' SourGraves are ready.")
    }

    override fun onDisable() {
        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }
}