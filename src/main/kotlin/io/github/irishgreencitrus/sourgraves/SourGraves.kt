package io.github.irishgreencitrus.sourgraves

import io.github.irishgreencitrus.sourgraves.config.GraveConfig
import io.github.irishgreencitrus.sourgraves.storage.*
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.milkbowl.vault2.economy.Economy
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bstats.charts.SingleLineChart
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

    private lateinit var metrics: Metrics

    var economy: Economy? = null
    var graveHandler = GraveHandler()
    var pluginConfig = GraveConfig()

    private val configFileName = "config.toml"
    private val bstatsPluginId = 26681

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

        try {
            val rsp = server.servicesManager.getRegistration(
                Economy::class.java
            )

            if (rsp == null) {
                return false
            }

            economy = rsp.provider
            return true
        } catch (e: NoClassDefFoundError) {
            logger.warning("Economy is enabled, but you are probably using Vault instead of VaultUnlocked.")
            return false
        }
    }

    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(GraveListener(), this)
        writeConfig(always = false)
        loadConfig()

        metrics = Metrics(this, bstatsPluginId)

        if (pluginConfig.economy.enable && !setupEconomy()) {
            logger.warning("Economy has been enabled, but VaultUnlocked is not installed correctly.")
            logger.warning("Disabling all economy features.")
            pluginConfig.economy.enable = false
        }


        var successfulStorage = false

        if (pluginConfig.forceLegacyFileStorage) {
            storage = LegacyFileStorage(dataFolder)
            successfulStorage = storage.init()
            if (!successfulStorage) {
                logger.severe(
                    "Legacy file based storage failed to load properly.\n" +
                            "This is not a bug with the plugin, rather a problem the config / filesystem.\n" +
                            "To prevent data loss, the plugin will now be disabled."
                )
                server.pluginManager.disablePlugin(this)
                return
            }
            logger.warning("You have enabled legacy json storage. The SQL settings will be ignored")
        }

        // SQL controls the SQL servers.
        if (pluginConfig.sql.enable && !successfulStorage) {
            if ("postgres" in pluginConfig.sql.jdbcConnectionUri) {
                storage = PostgresStorage()
            } else if ("mysql" in pluginConfig.sql.jdbcConnectionUri) {
                storage = MySqlStorage()
            } else {
                logger.severe(
                    "You have enabled SQL but you have an unsupported connection uri.\n" +
                            "Supported servers are PostgreSQL and MySQL.\n" +
                            "To prevent data loss, the plugin will be disabled."
                )
                server.pluginManager.disablePlugin(this)
                return
            }
            successfulStorage = storage.init()
            if (!successfulStorage) {
                logger.severe(
                    "The SQL database server failed to load correctly.\n" +
                            "This is not a bug with the plugin, rather a problem the config / SQL server.\n" +
                            "To prevent data loss, the plugin will be disabled."
                )
                server.pluginManager.disablePlugin(this)
                return
            }
        }

        if (!::storage.isInitialized || !successfulStorage) {
            storage = SQLiteStorage()
            successfulStorage = storage.init()
            if (!successfulStorage) {
                logger.severe(
                    "The SQLite database server failed to load correctly.\n" +
                            "This is not a bug with the plugin, rather a problem the config / filesystem.\n" +
                            "To prevent data loss, the plugin will be disabled."
                )
                server.pluginManager.disablePlugin(this)
                return
            }
        }

        if ((!pluginConfig.sql.alreadyConvertedFromJson) && (storage is SQLStorage)) {
            val dataFile = File(dataFolder, "graves.json")
            if (dataFile.exists()) {
                logger.warning("Converting `graves.json` to your enabled SQL storage. This will only happen once.")
                val oldStorage = LegacyFileStorage(dataFolder)
                if (oldStorage.init()) {
                    (storage as SQLStorage).convertFrom(oldStorage)
                    pluginConfig.sql.alreadyConvertedFromJson = true
                    writeConfig(true)
                } else {
                    logger.warning("Failed to convert graves from `graves.json` to the SQL storage.")
                }
            }
        }



        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(GraveCommand.createCommand().build())
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, {
                if (pluginConfig.deleteInMinutes > 0)
                    storage.cleanupHardExpiredGraves()
                storage.sync()
                if (pluginConfig.logMessages.cleanupTask)
                    logger.info("Cleaned graves and written to disk")
            },
            pluginConfig.periodicCleanupDelayMinutes.toLong() * 60 * 20,
            pluginConfig.periodicCleanupPeriodMinutes.toLong() * 60 * 20
        )
        if (pluginConfig.logMessages.startupMessage) {
            logger.info("Thanks for using my plugin, report any bugs you find at https://github.com/irishgreencitrus/SourGraves/issues")
            logger.info("Feel free to also join the Discord for help at https://discord.gg/B7Sd3eaTrs")
        }


        metrics.addCustomChart(SimplePie("storageType") {
            return@SimplePie when (storage) {
                is SQLiteStorage -> "SQLite"
                is MySqlStorage -> "MySQL"
                is PostgresStorage -> "Postgres"
                is LegacyFileStorage -> "Legacy"
                else -> "Other"
            }
        })

        metrics.addCustomChart(SingleLineChart("graveCount") {
            storage.count()
        })

        logger.info("irishgreencitrus' SourGraves are ready.")
    }


    override fun onDisable() {
        writeConfig(always = true)

        storage.sync()

        logger.info("irishgreencitrus' SourGraves have been disabled.")
    }
}