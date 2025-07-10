package io.github.irishgreencitrus.sourgraves.sql

object Database {
    /*
    @OptIn(ExperimentalSerializationApi::class)
    fun convertCurrentGravesToDatabase() {
        Class.forName("org.postgresql.Driver")
        //val cfg = SourGraves.plugin.pluginConfig.sql
        val cfg = SqlConfig()
        try {

            DriverManager.getConnection(
                cfg.jdbcConnectionUri,
                cfg.username,
                cfg.password
            ).use { conn ->

                val exists = conn.isValid(0)

                SourGraves.plugin.logger.warning("DB connection exists? $exists")
                conn.createStatement().use { stmt ->
                    stmt.execute(
                        """CREATE TABLE IF NOT EXISTS graves (
                |    graveUuid varchar(36) NOT NULL,
                |    items bytea NOT NULL,
                |    ownerUuid varchar(36) NOT NULL,
                |    createdAtUnixTime bigint NOT NULL,
                |    linkedArmourStandUuid varchar(36) NOT NULL,
                |    locationWorld text NOT NULL,
                |    locationX double precision NOT NULL,
                |    locationY double precision NOT NULL,
                |    locationZ double precision NOT NULL,
                |    PRIMARY KEY (graveUuid)
                |);""".trimMargin()
                    )
                }

                val insertQuery =
                    "INSERT INTO graves (graveUuid, items, ownerUuid, createdAtUnixTime, linkedArmourStandUuid, locationWorld, locationX, locationY, locationZ)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
                for (g in SourGraves.plugin.graveHandler.graves.entries) {
                    val itemStack = ProtoBuf { encodeDefaults = true }
                        .encodeToByteArray(
                            g.value.items
                            .mapIndexed { index, itemStack -> index to itemStack }
                            .toMap()
                            .filterValues { it != null }
                        )

                    var skip = false
                    conn.prepareStatement("SELECT COUNT(*) FROM graves WHERE graveUuid = ?").use { stmt ->
                        stmt.setString(1, g.key.toString())
                        val result = stmt.executeQuery()
                        if (result.getInt(0) >= 0) {
                            skip = true
                        }
                    }

                    if (skip) continue

                    conn.prepareStatement(insertQuery).use { stmt ->
                        stmt.setString(1, g.key.toString())
                        stmt.setBytes(2, itemStack)
                        stmt.setString(3, g.value.ownerUuid.toString())
                        stmt.setLong(4, g.value.createdAt.epochSecond)
                        stmt.setString(5, g.value.linkedArmourStandUuid.toString())
                        stmt.setString(6, g.value.cachedLocation.world?.name)
                        stmt.setDouble(7, g.value.cachedLocation.x)
                        stmt.setDouble(8, g.value.cachedLocation.y)
                        stmt.setDouble(9, g.value.cachedLocation.z)
                        stmt.executeUpdate()
                    }
                    SourGraves.plugin.logger.info("Processed grave ${g.key}")
                }

            }
        } catch (e: SQLException) {
            SourGraves.plugin.logger.severe(e.message ?: "null")
        }
    }
     */
}