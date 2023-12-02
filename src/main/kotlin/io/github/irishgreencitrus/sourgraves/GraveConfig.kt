package io.github.irishgreencitrus.sourgraves

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import java.io.File

@Serializable
data class GraveConfig(
    val expiryMinutes: Int = 5,
    val hardExpiryMinutes: Int = 30,
    val maxGraves: Int = 3,
    val dropItemsOnTooManyGraves: Boolean = true,
    val dropItemsOnHardExpiry: Boolean = true,
    val recoverParticle: String = "minecraft:soul",
    val recoverParticleAmount: Int = 1000,
    val recoverSound: String = "minecraft:block.respawn_anchor.deplete",
    val periodicCleanupDelayMinutes: Int = 10,
    val periodicCleanupPeriodMinutes: Int = 5,
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
}
