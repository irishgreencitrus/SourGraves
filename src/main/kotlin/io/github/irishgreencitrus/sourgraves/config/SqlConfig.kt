package io.github.irishgreencitrus.sourgraves.config

import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

enum class SqlServers {
    SQLite,
    MySQL,
    Oracle,
    Postgres
}

@Serializable
data class SqlConfig(
    @TomlComment(
        "Whether to enable connecting to a SQL server to store graves, instead of the JSON file.\n" +
                "Useful for very servers where a lot of graves are being created."
    )
    var enable: Boolean = true,
    var serverType: SqlServers = SqlServers.SQLite
)