package io.github.irishgreencitrus.sourgraves.config

import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
data class LogMessagesConfig(
    @TomlComment("Whether to enable the GitHub and Discord message on startup.")
    var startupMessage: Boolean = true,
    @TomlComment(
        "Whether to log every time the cleanup task runs. Useful for debugging configuration.\n" +
                "Not recommended to leave on for prolonged periods of time."
    )
    var cleanupTask: Boolean = false,
    @TomlComment(
        "Whether to log if a grave hasn't stored its location. Can occur for graves created before v1.5.0.\n" +
                "It is recommended to leave this enabled as it will only be printed once anyway."
    )
    var graveMissingLocation: Boolean = true,
    @TomlComment(
        "Gives a warning if a grave that is about to be deleted has its chunk loaded but the armour stand (The player head) has been deleted.\n" +
                "This could be a sign that another plugin is deleting armour stands."
    )
    var armourStandNotFoundOnGravePurge: Boolean = true,
    @TomlComment("Gives a warning on startup if you are using file-based storage and have more than 100 graves")
    var moreThan100GravesWarning: Boolean = true,
)