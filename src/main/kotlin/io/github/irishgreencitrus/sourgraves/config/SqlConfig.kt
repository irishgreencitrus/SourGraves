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
    var jdbcConnectionUri: String = "jdbc:postgres:",
    var username: String = "admin",
    var password: String = "changeme",
    @TomlComment(
        "Soft deletion will mean all grave data will stay in the database, and only ever be marked as deleted.\n" +
                "This provides extra protection against item loss and allows complete recovery.\n" +
                "Disabling this will not retroactively apply, and merely actually delete graves made after the change."
    )
    var softDeletion: Boolean = true,
    var alreadyConvertedFromJson: Boolean = false,
)