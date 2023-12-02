package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.persist.GravePersist
import io.github.irishgreencitrus.sourgraves.persist.GravePersistJSON
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SourGraves : JavaPlugin() {
    companion object {
        val plugin: SourGraves get() {
            return getPlugin(SourGraves::class.java)
        }
    }

    val gravePersist: GravePersist = GravePersistJSON(File(dataFolder, "graves.json"))
    var graveHandler = GraveHandler(gravePersist)
    lateinit var pluginConfig: GraveConfig
    fun initConfig() {
        val configFile = File(dataFolder, "config.toml")
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.writeText(GraveConfig().toString())
        }
        pluginConfig = GraveConfig.fromFile(configFile)
    }

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(GraveListener(), this)
        // Starts after 10 minutes, clears graves every 5
        gravePersist.initialize()
        graveHandler.load()
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
                graveHandler.cleanupHardExpiredGraves()
        }, 10 * 60 * 20, 5 * 60 * 20)
        initConfig()
        logger.info("irishgreencitrus' SourGraves are ready.")
    }


    override fun onDisable() {
        graveHandler.save()
        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }
}