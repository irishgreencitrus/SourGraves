package io.github.irishgreencitrus.sourgraves.config

import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
data class SqlConfig(
    @TomlComment(
        "Whether to enable connecting to a SQL server to store graves, instead of the JSON file.\n" +
                "Useful for very large servers where a lot of graves are being created."
    )
    var enable: Boolean = false,
    @TomlComment(
        "Supported SQL servers: MariaDB and PostgreSQL\n" +
                "Google 'jdbc connection URL' to find out what to put here"
    )
    var jdbcConnectionUri: String = "jdbc:sqlite:./graves.db",
    @TomlComment(
        "You can either use an tomlAuthFile or supply a username and password here.\n" +
                "The auth file takes priority, so leave it blank if you don't want it to be used"
    )
    var tomlAuthFile: String = "",
    var username: String = "admin",
    var password: String = "changeme",
    var alreadyConvertedFromJson: Boolean = false,
)