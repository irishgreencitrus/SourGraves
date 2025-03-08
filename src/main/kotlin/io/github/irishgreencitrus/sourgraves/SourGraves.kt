package io.github.irishgreencitrus.sourgraves

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SourGraves : JavaPlugin() {
    companion object {
        val plugin: SourGraves get() {
            return getPlugin(SourGraves::class.java)
        }
    }

    var graveHandler = GraveHandler()
    lateinit var pluginConfig: GraveConfig

    private fun initConfig() {
        val configFile = File(dataFolder, "config.toml")
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.writeText(GraveConfig().toFileString())
        }
        pluginConfig = GraveConfig.fromFile(configFile)
    }


    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(GraveListener(), this)
        // Starts after 10 minutes, clears graves every 5
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
                graveHandler.cleanupHardExpiredGraves()
        }, 10 * 60 * 20, 5 * 60 * 20)
        initConfig()
        graveHandler.loadGravesFile(dataFolder)
        if (pluginConfig.resetTimeoutOnStop) {
            graveHandler.resetGraveTimers()
        }
        logger.info("irishgreencitrus' SourGraves are ready.")
    }


    override fun onDisable() {
        graveHandler.writeGravesFile(dataFolder)
        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }
}