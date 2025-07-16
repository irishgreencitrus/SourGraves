package io.github.irishgreencitrus.sourgraves.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.irishgreencitrus.sourgraves.GraveData
import io.github.irishgreencitrus.sourgraves.SourGraves
import io.github.irishgreencitrus.sourgraves.serialize.ItemStackSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant
import java.util.*

// This is basically the same as PostgresStorage,
// but SQLite doesn't support all the datatypes.
// Forgive me gods of D.R.Y.
@OptIn(ExperimentalSerializationApi::class)
class SQLiteStorage : SQLStorage() {
    private lateinit var ds: HikariDataSource
    private val itemSer = ProtoBuf { encodeDefaults = true }

    private val createStatement: String =
        """
            CREATE TABLE IF NOT EXISTS graves (
                graveUuid TEXT PRIMARY KEY NOT NULL,
                items BLOB NOT NULL,
                ownerUuid TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                timerStartedAtGameTime INTEGER NOT NULL,
                linkedArmourStandUuid TEXT NOT NULL,
                locationWorld TEXT NOT NULL,
                locationX REAL NOT NULL,
                locationY REAL NOT NULL,
                locationZ REAL NOT NULL,
                deletedAt INTEGER
            );
        """.trimIndent()

    override fun init(): Boolean {
        Class.forName("org.sqlite.JDBC")

        val poolConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:plugins/SourGraves/graves.db"
            maximumPoolSize = 1
            isAutoCommit = true
            connectionTestQuery = "SELECT 1"
        }

        ds = HikariDataSource(poolConfig)

        try {
            ds.connection.use { conn ->
                if (!conn.isValid(0)) return false
                conn.createStatement().use { stmt ->
                    stmt.execute(createStatement)
                }
            }
        } catch (e: SQLException) {
            SourGraves.plugin.logger.severe("SQL init failed '${e.message}'")
            return false
        }
        return true
    }

    override fun contains(uuid: UUID): Boolean {
        val sql = "SELECT 1 FROM graves WHERE graveUuid = ? AND deletedAt IS NULL LIMIT 1"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    return rs.next()
                }
            }
        }
    }

    override fun count(): Int {
        val sql = "SELECT COUNT(*) FROM graves WHERE deletedAt IS NULL"
        ds.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    return if (rs.next()) {
                        rs.getInt(1)
                    } else {
                        0
                    }
                }
            }
        }
    }

    override fun query(uuid: UUID): GraveData? {
        val sql = "SELECT * FROM graves WHERE graveUuid = ? AND deletedAt IS NULL LIMIT 1"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) {
                        rs.toGraveData()
                    } else {
                        null
                    }
                }
            }
        }

    }

    override fun write(uuid: UUID, data: GraveData) {
        val insertSql: String = """
            INSERT INTO graves (
                graveUuid,
                items,
                ownerUuid,
                createdAt,
                timerStartedAtGameTime,
                linkedArmourStandUuid,
                locationWorld,
                locationX,
                locationY,
                locationZ,
                deletedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        ds.connection.use { conn ->
            conn.prepareStatement(insertSql).use { stmt ->
                var i = 1
                stmt.setString(i++, uuid.toString())
                stmt.setBytes(i++, itemStackListToByteArray(data.items))
                stmt.setString(i++, data.ownerUuid.toString())
                stmt.setLong(i++, data.createdAt.toEpochMilli())
                stmt.setLong(i++, data.timerStartedAtGameTime)
                stmt.setString(i++, data.linkedArmourStandUuid.toString())

                val loc = data.cachedLocation
                stmt.setString(i++, loc.world?.name ?: "")
                stmt.setDouble(i++, loc.x)
                stmt.setDouble(i++, loc.y)
                stmt.setDouble(i++, loc.z)

                if (data.deletedAt != null)
                    stmt.setLong(i, data.deletedAt.toEpochMilli())

                stmt.executeUpdate()
            }

        }
    }

    override fun updateItems(uuid: UUID, items: List<ItemStack?>) {
        val updateSql = """
            UPDATE graves
            SET items = ?
            WHERE graveUuid = ?
        """.trimIndent()
        ds.connection.use { conn ->
            conn.prepareStatement(updateSql).use { stmt ->
                var i = 1
                stmt.setBytes(i++, itemStackListToByteArray(items))
                stmt.setString(i, uuid.toString())

                stmt.executeUpdate()
            }
        }
    }

    private fun itemStackListToByteArray(list: List<ItemStack?>): ByteArray {
        val map = list.mapIndexedNotNull { index, itemStack ->
            itemStack?.let { index to itemStack }
        }.toMap()
        return itemSer.encodeToByteArray(
            MapSerializer(Int.serializer(), ItemStackSerializer),
            map
        )
    }

    private fun ByteArray.toItemStackList(): List<
            @Serializable(with = ItemStackSerializer::class)
            ItemStack?> {
        val map: Map<Int, ItemStack> = itemSer.decodeFromByteArray(
            MapSerializer(Int.serializer(), ItemStackSerializer),
            this
        )
        return (0..<43).map { idx ->
            map[idx]
        }.toList()
    }

    override fun delete(uuid: UUID) {
        val softDelete = SourGraves.plugin.pluginConfig.sql.softDeletion
        if (softDelete) {
            val sql = "UPDATE graves SET deletedAt = ? WHERE graveUuid = ?"
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, Instant.now().toEpochMilli())
                    stmt.setString(2, uuid.toString())
                    stmt.executeUpdate()
                }
            }
        } else {
            val sql = "DELETE FROM graves WHERE graveUuid = ?"
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, uuid.toString())
                    stmt.executeUpdate()
                }
            }
        }
    }

    override fun searchPlayerGraves(playerUUID: UUID, dimension: String?): Map<UUID, GraveData> {
        val sql = if (dimension == null)
            "SELECT * FROM graves WHERE ownerUuid = ? AND deletedAt IS NULL"
        else
            "SELECT * FROM graves WHERE ownerUuid = ? AND locationWorld = ? AND deletedAt IS NULL"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, playerUUID.toString())
                if (dimension != null)
                    stmt.setString(2, dimension)
                stmt.executeQuery().use { rs ->
                    val ret: HashMap<UUID, GraveData> = hashMapOf()
                    while (rs.next()) {
                        val uuid = UUID.fromString(rs.getString("graveUuid"))
                        val gd = rs.toGraveData()
                        ret[uuid] = gd
                    }
                    return ret
                }
            }
        }
    }

    override fun oldestGrave(playerUUID: UUID, dimension: String?): Pair<UUID, GraveData>? {
        val sql = if (dimension == null)
            "SELECT * FROM graves WHERE ownerUuid = ? AND deletedAt IS NULL ORDER BY createdAt ASC LIMIT 1"
        else
            "SELECT * FROM graves WHERE ownerUuid = ? AND locationWorld = ? AND deletedAt IS NULL ORDER BY createdAt ASC LIMIT 1"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, playerUUID.toString())
                if (dimension != null)
                    stmt.setString(2, dimension)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        val uuid = UUID.fromString(rs.getString("graveUuid"))
                        val gd = rs.toGraveData()
                        return Pair(uuid, gd)
                    }
                }
            }
        }
        return null
    }

    override fun newestGrave(playerUUID: UUID, dimension: String?): Pair<UUID, GraveData>? {
        val sql = if (dimension == null)
            "SELECT * FROM graves WHERE ownerUuid = ? AND deletedAt IS NULL ORDER BY createdAt DESC LIMIT 1"
        else
            "SELECT * FROM graves WHERE ownerUuid = ? AND locationWorld = ? AND deletedAt IS NULL ORDER BY createdAt DESC LIMIT 1"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, playerUUID.toString())
                if (dimension != null)
                    stmt.setString(2, dimension)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        val uuid = UUID.fromString(rs.getString("graveUuid"))
                        val gd = rs.toGraveData()
                        return Pair(uuid, gd)
                    }
                }
            }
        }
        return null
    }

    override fun cleanupHardExpiredGraves() {
        val sql = """
            SELECT graveUuid
            FROM graves
            WHERE timerStartedAtGameTime <= ?
        """.trimIndent()
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(
                    1,
                    SourGraves.plugin.server.worlds.first().gameTime - SourGraves.plugin.pluginConfig.deleteInMinutes * 60 * 20
                )

                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val uuid = rs.getString(1)
                        SourGraves.plugin.graveHandler.purgeGraveDropItems(UUID.fromString(uuid))
                    }
                }
            }
        }
    }


    private fun ResultSet.toGraveData(): GraveData {
        return GraveData(
            ownerUuid = UUID.fromString(getString("ownerUuid")),
            linkedArmourStandUuid = UUID.fromString(getString("linkedArmourStandUuid")),
            createdAt = Instant.ofEpochMilli(getLong("createdAt")),
            timerStartedAtGameTime = getLong("timerStartedAtGameTime"),
            items = getBytes("items").toItemStackList(),
            cachedLocation = Location(
                Bukkit.getWorld(getString("locationWorld")),
                getDouble("locationX"),
                getDouble("locationY"),
                getDouble("locationZ"),
            ),
            deletedAt = if (wasNull()) null else Instant.ofEpochMilli(getLong("deletedAt"))
        )
    }
}