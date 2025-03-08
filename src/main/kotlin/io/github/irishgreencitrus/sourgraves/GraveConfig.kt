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
    @TomlComment("After this many minutes, the grave will be accessible by all players")
    val publicInMinutes: Int = 5,
    @TomlComment("After this many minutes, the grave will be deleted")
    val deleteInMinutes: Int = 30,
    @TomlComment("The maximum number of graves a player can have.\n"
            + "After this limit, a player's oldest grave will be deleted")
    val maxGravesPerPlayer: Int = 3,
    @TomlComment("Whether to drop the items of the oldest grave once a player has exceeded max graves")
    val dropItemsOnTooManyGraves: Boolean = true,
    @TomlComment("Whether to drop the items of a just-deleted grave. If false, the items just disappear")
    val dropItemsOnGraveDeletion: Boolean = true,
    @TomlComment("The particle to use for the grave recovery animation")
    val recoverParticle: String = Particle.SOUL.name,
    @TomlComment("The number of particles to use in the grave recovery animation")
    val recoverParticleAmount: Int = 1000,
    @TomlComment("The sound to play on grave recovery")
    val recoverSound: String = "minecraft:block.respawn_anchor.deplete",
    @TomlComment("When should the first grave cleanup run, after the server has started")
    val periodicCleanupDelayMinutes: Int = 10,
    @TomlComment("How frequently the grave cleanup runs")
    val periodicCleanupPeriodMinutes: Int = 5,
    @TomlComment("Whether to reset the grave timeout when the server stops")
    val resetTimeoutOnStop: Boolean = false,
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
