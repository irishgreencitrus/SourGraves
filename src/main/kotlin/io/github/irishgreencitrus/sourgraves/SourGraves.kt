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
        Bukkit.getPluginManager().registerEvents(GraveListener(), this)
        logger.info("irishgreencitrus' SourGraves are ready.")
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
                graveHandler.cleanupHardExpiredGraves()
            }, 20*10*60, 5*60*20)
    }

    override fun onDisable() {
        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }
}