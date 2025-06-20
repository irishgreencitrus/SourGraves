package io.github.irishgreencitrus.sourgraves

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlComment
import org.bukkit.Particle
import java.io.File

@Serializable
data class GraveConfig(
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
    @TomlComment("Whether to reset the grave timeout when the server stops")
    var resetTimeoutOnStop: Boolean = false,
    @TomlComment("Whether to send a player their grave coordinates once they respawn")
    var notifyCoordsOnRespawn: Boolean = false,
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
            # A player without any items in their inventory will not drop a grave.
        """.trimIndent() + toString()
    }
}
