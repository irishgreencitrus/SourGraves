package io.github.irishgreencitrus.sourgraves.config

import io.github.irishgreencitrus.sourgraves.SourGraves
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlComment
import org.bukkit.Particle
import java.io.File

@Serializable
data class GraveConfig(
    @TomlComment("There is no need to edit this manually, it just tells the plugin whether to rewrite the config after it has been updated.")
    var configVersion: Int = SourGraves.CONFIG_VERSION,
    @TomlComment(
        "After this many minutes, the grave will be accessible by all players.\n" +
                "Set to `-1` to disable this."
    )
    var publicInMinutes: Int = 5,
    @TomlComment(
        "After this many minutes, the grave will be deleted.\n" +
                "Set to `-1` to disable this."
    )
    var deleteInMinutes: Int = 30,
    @TomlComment("The maximum number of graves a player can have.\n"
            + "After this limit, a player's oldest grave will be deleted.\n"
            + "Set to `-1` to disable this"
    )
    var maxGravesPerPlayer: Int = 3,
    @TomlComment("Whether to prevent graves being created for PvP kills")
    var disableForPvpKills: Boolean = false,
    @TomlComment("Whether to drop the items of the oldest grave once a player has exceeded max graves")
    var dropItemsOnTooManyGraves: Boolean = true,
    @TomlComment("Whether to drop the items of a just-deleted grave. If false, the items just disappear")
    var dropItemsOnGraveDeletion: Boolean = true,
    @TomlComment("The particle to use for the grave recovery animation")
    var recoverParticle: String = Particle.SOUL.name,
    @TomlComment("The number of particles to use in the grave recovery animation")
    var recoverParticleAmount: Int = 1000,
    @TomlComment("The sound to play on grave recovery")
    var recoverSound: String = "minecraft:block.respawn_anchor.deplete",
    @TomlComment("When should the first grave cleanup run, after the server has started")
    var periodicCleanupDelayMinutes: Int = 10,
    @TomlComment("How frequently the grave cleanup runs")
    var periodicCleanupPeriodMinutes: Int = 5,
    @TomlComment("Whether to send a player their grave coordinates once they respawn")
    var notifyCoordsOnRespawn: Boolean = false,
    @TomlComment("If this is `true`, the default Right-Click will now open an inventory.\nYou can use Shift-Right-Click to restore the grave as normal.")
    var allowChestLikeGraveAccess: Boolean = false,
    @TomlComment("Swaps the behaviour of normal and shift clicking when `allowChestLikeGraveAccess` is `true`")
    var chestAccessSwapNormalAndShift: Boolean = false,
    @TomlComment(
        "Whether to use legacy json-based file storage rather than the new SQLite database.\n" +
                "I can't think of any reason to enable this, but the functionality remains, so the option also remains.\n" +
                "Overrides any settings in [sql]."
    )
    var forceLegacyFileStorage: Boolean = false,
    @TomlComment(
        "A list of the worlds that graves will *not* spawn in.\n" +
                "This will lead to the default behaviour i.e. dropping items.\n" +
                "Example values: ['world_nether']"
    )
    var disabledWorlds: List<String> = listOf(),
    @TomlComment(
        "Changing the [sql] options require you to restart the server.\n" +
                "NOTE: this only controls the supported SQL servers. SQLite storage is enabled by default and does not require any configuration.\n"
    )
    var sql: SqlConfig = SqlConfig(),
    var economy: EconomyConfig = EconomyConfig(),
    @TomlComment(
        "Change anything in here if you find the log messages too invasive.\n" +
                "Most severe and warning messages cannot be disabled, by design."
    )
    var logMessages: LogMessagesConfig = LogMessagesConfig()
) {
    companion object {
        fun fromFile(f: File): GraveConfig {
            val tomlAsString = f.readText()
            val t = Toml {
                explicitNulls = true
                ignoreUnknownKeys = true
            }
            return t.decodeFromString<GraveConfig>(tomlAsString)
        }
    }

    override fun toString(): String {
        return Toml.encodeToString(this)
    }

    fun toFileString(): String {
        return """
            # irishgreencitrus' Sour Graves
            # Everything in the plugin is completely configurable.
            # If you mess up the file, you can delete it to reset it to the defaults.
            #
            # Some key notes:
            #  - A player without any items in their inventory will not drop a grave.
            #  - A player with keepInventory enabled will not drop a grave.
            #  - Comments made in this file will be deleted.
            #  - The cleanup task is what actually deletes graves, as well as saves graves to disk.
            #     It should not be ran too infrequently, as your graves will not be saved if the server crashes.
            #     The default should probably be fine, but it can be raised or lowered as necessary.
            #
            
        """.trimIndent() + toString()
    }
}
