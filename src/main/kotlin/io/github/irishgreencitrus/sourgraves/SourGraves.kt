package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.config.GraveConfig
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.milkbowl.vault2.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class SourGraves : JavaPlugin() {
    companion object {
        val plugin: SourGraves get() {
            return getPlugin(SourGraves::class.java)
        }
        val economy: Economy?
            get() {
                return plugin.economy
            }
        const val CONFIG_VERSION = 1
    }

    var economy: Economy? = null
    var graveHandler = GraveHandler()
    var pluginConfig = GraveConfig()

    private val configFileName = "config.toml"

    fun writeConfig(always: Boolean) {
        val configFile = File(dataFolder, configFileName)
        if (!configFile.exists() || always) {
            configFile.parentFile.mkdirs()
            configFile.writeText(pluginConfig.toFileString())
        }
    }

    fun loadConfig() {
        val configFile = File(dataFolder, configFileName)
        pluginConfig = GraveConfig.fromFile(configFile)
        if (pluginConfig.configVersion != CONFIG_VERSION) {
            pluginConfig.configVersion = CONFIG_VERSION
            writeConfig(true)
        }
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }

        val rsp = server.servicesManager.getRegistration(
            Economy::class.java
        )

        if (rsp == null) {
            return false
        }

        economy = rsp.provider
        return true
    }


    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(GraveListener(), this)
        writeConfig(always = false)
        loadConfig()
        if (!setupEconomy() && pluginConfig.economy.enable) {
            logger.warning("Economy has been enabled, but Vault is not installed correctly.")
            logger.warning("Disabling all economy features.")
            pluginConfig.economy.enable = false;
        }
        graveHandler.loadGravesFile(dataFolder)
        if (pluginConfig.resetTimeoutOnStop) {
            graveHandler.resetGraveTimers()
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(GraveCommand.createCommand().build())
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, {
                graveHandler.cleanupHardExpiredGraves()
                graveHandler.writeGravesFile(dataFolder)
                if (pluginConfig.logCleanupTaskRuns)
                    logger.info("Cleaned graves and written to disk")
            },
            pluginConfig.periodicCleanupDelayMinutes.toLong() * 60 * 20,
            pluginConfig.periodicCleanupPeriodMinutes.toLong() * 60 * 20
        )
        logger.info("irishgreencitrus' SourGraves are ready.")
    }


    override fun onDisable() {
        writeConfig(always = true)
        graveHandler.writeGravesFile(dataFolder)
        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }

    fun currency(): String? {
        return economy?.getDefaultCurrency(name)
    }
}