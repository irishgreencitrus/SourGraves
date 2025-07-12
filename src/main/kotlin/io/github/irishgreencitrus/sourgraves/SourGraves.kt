package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.config.GraveConfig
import io.github.irishgreencitrus.sourgraves.storage.FileBackedStorage
import io.github.irishgreencitrus.sourgraves.storage.GraveStorage
import io.github.irishgreencitrus.sourgraves.storage.PostgresStorage
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
        val storage: GraveStorage
            get() {
                return plugin.storage
            }
        const val CONFIG_VERSION = 2
    }

    lateinit var storage: GraveStorage

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

    enum class StorageFailure {
        NONE,
        SQL,
        FILE,
    }

    var storageFailure: StorageFailure = StorageFailure.NONE

    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(GraveListener(), this)
        writeConfig(always = false)
        loadConfig()
        if (!setupEconomy() && pluginConfig.economy.enable) {
            logger.warning("Economy has been enabled, but Vault is not installed correctly.")
            logger.warning("Disabling all economy features.")
            pluginConfig.economy.enable = false
        }


        // TODO: load specific storage here.
        var successfulStorage = false
        if (pluginConfig.sql.enable) {
            if ("postgres" in pluginConfig.sql.jdbcConnectionUri) {
                storage = PostgresStorage()
            } else {
                TODO("Add MySQL here")
            }
            successfulStorage = storage.init()
            if (!pluginConfig.sql.alreadyConvertedFromJson) {
                // TODO: add the conversion process here
                pluginConfig.sql.alreadyConvertedFromJson = true
            }
            if (!successfulStorage) {
                logger.severe(
                    "The SQL database failed to load correctly. `graves.json` will be used as backup.\n" +
                            "Support will not be provided if this message appears in your log.\n" +
                            "The plugin will now shutdown."
                )
                storageFailure = StorageFailure.SQL
                server.pluginManager.disablePlugin(this)
                return
            }
        }

        if (!::storage.isInitialized || !successfulStorage) {
            storage = FileBackedStorage(dataFolder)
            successfulStorage = storage.init()
            if (!successfulStorage) {
                logger.severe(
                    "File based storage failed to load properly.\n" +
                            "Support will not be provided if this message appears in your log.\n" +
                            "The plugin will now shutdown."
                )
                storageFailure = StorageFailure.FILE
                server.pluginManager.disablePlugin(this)
                return
            }
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(GraveCommand.createCommand().build())
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, {
                storage.cleanupHardExpiredGraves()
                storage.sync()
                if (pluginConfig.logCleanupTaskRuns)
                    logger.info("Cleaned graves and written to disk")
            },
            pluginConfig.periodicCleanupDelayMinutes.toLong() * 60 * 20,
            pluginConfig.periodicCleanupPeriodMinutes.toLong() * 60 * 20
        )
        logger.info("irishgreencitrus' SourGraves are ready.")
        val a = server.worlds[0].gameTime
    }


    override fun onDisable() {
        writeConfig(always = true)

        storage.sync()

        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }
}