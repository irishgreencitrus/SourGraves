# irishgreencitrus' Sour Graves

- Save your items from being deleted after a death!
- Items are always saved no matter how you die!
- Recover items by clicking on the grave
- Graves become public (raidable) after a timer (5 mins by default)
- Graves are deleleted with their items dropped on the ground after a timer (30 mins by default)

## Default Configuration

```toml
#   irishgreencitrus' Sour Graves v2 config
#
#
##############################################
# COMMENTS MADE IN THIS FILE WILL BE DELETED #
##############################################
#
#
# Notable updates since v1:
# - Graves are now stored in `graves.db` instead of `graves.json`.
# - If you have an existing `graves.json`, they should be converted to the new format.
# - Postgres and MySQL are also supported as external databases.
#
# Everything in the plugin is completely configurable.
# If you mess up the file, you can delete it to reset it to the defaults.
#
# Some key notes:
#  - A player without any items in their inventory will not drop a grave.
#  - A player with keepInventory enabled will not drop a grave.
#  - The cleanup task is what actually deletes graves, as well as saves graves to disk.
#     It should not be ran too infrequently, as your graves will not be saved if the server crashes.
#     The default should probably be fine, but it can be raised or lowered as necessary.
## There is no need to edit this manually, it just tells the plugin whether to rewrite the config after it has been updated.
configVersion = 2
# After this many minutes, the grave will be accessible by all players.
# Set to `-1` to disable this.
publicInMinutes = 5
# After this many minutes, the grave will be deleted.
# Set to `-1` to disable this.
deleteInMinutes = 30
# The maximum number of graves a player can have.
# After this limit, a player's oldest grave will be deleted.
# Set to `-1` to disable this
maxGravesPerPlayer = 3
# Whether to prevent graves being created for PvP kills
disableForPvpKills = false
# Whether to drop the items of the oldest grave once a player has exceeded max graves
dropItemsOnTooManyGraves = true
# Whether to drop the items of a just-deleted grave. If false, the items just disappear
dropItemsOnGraveDeletion = true
# The particle to use for the grave recovery animation
recoverParticle = "SOUL"
# The number of particles to use in the grave recovery animation
recoverParticleAmount = 1000
# The sound to play on grave recovery
recoverSound = "minecraft:block.respawn_anchor.deplete"
# When should the first grave cleanup run, after the server has started
periodicCleanupDelayMinutes = 10
# How frequently the grave cleanup runs
periodicCleanupPeriodMinutes = 5
# Whether to send a player their grave coordinates once they respawn
notifyCoordsOnRespawn = false
# If this is `true`, the default Right-Click will now open an inventory.
# You can use Shift-Right-Click to restore the grave as normal.
allowChestLikeGraveAccess = false
# Swaps the behaviour of normal and shift clicking when `allowChestLikeGraveAccess` is `true`
chestAccessSwapNormalAndShift = false
# Whether to use legacy json-based file storage rather than the new SQLite database.
# I can't think of any reason to enable this, but the functionality remains, so the option also remains.
# Overrides any settings in [sql].
forceLegacyFileStorage = false
# A list of the worlds that graves will *not* spawn in.
# This will lead to the default behaviour i.e. dropping items.
# Example values: ['world_nether']
disabledWorlds = []

# Changing the [sql] options require you to restart the server.
# NOTE: this only controls the supported SQL servers.
# SQLite storage is enabled by default and does not require any configuration.

[sql]
# Whether to enable connecting to a SQL server to store graves, instead of the JSON file.
# Useful for very large servers where a lot of graves are being created.
enable = false
# Supported SQL servers: MySQL and PostgreSQL
# Google 'jdbc connection URL' to find out what to put here
jdbcConnectionUri = "jdbc:postgres:admin@localhost/sourgraves"
username = "admin"
password = "changeme"
# Soft deletion will mean all grave data will stay in the database, and only ever be marked as deleted.
# This provides extra protection against item loss and allows complete recovery.
# Disabling this will not retroactively apply, and merely actually delete graves made after the change.
softDeletion = true
alreadyConvertedFromJson = true

[economy]
# Whether to enable the economy features of this plugin (Requires VaultUnlocked to be installed)
enable = false
# The cost to recover one of your own graves
graveRecoverCost = 1.0
# The cost either per grave or per item stack in the grave
graveRecoverPaymentType = "FLAT"
# The cost to rob someone else's grave
graveRobCost = 5.0
# The cost either per grave or per item stack in the grave
graveRobPaymentType = "FLAT"

# Change anything in here if you find the log messages too invasive.
# Most severe and warning messages cannot be disabled, by design.

[logMessages]
# Whether to enable the GitHub and Discord message on startup.
startupMessage = true
# Whether to log every time the cleanup task runs. Useful for debugging configuration.
# Not recommended to leave on for prolonged periods of time.
cleanupTask = false
# Whether to log if a grave hasn't stored its location. Can occur for graves created before v1.5.0.
# It is recommended to leave this enabled as it will only be printed once anyway.
graveMissingLocation = true
# Gives a warning if a grave that is about to be deleted has its chunk loaded but the armour stand (The player head) has been deleted.
# This could be a sign that another plugin is deleting armour stands.
armourStandNotFoundOnGravePurge = true
# Gives a warning on startup if you are still using legacy JSON storage and have more than 100 graves.
moreThan100GravesWarning = true
```

## Permissions

```yml
sourgraves.admin.server:
  description: Allows you to use `/sourgraves disk` and `/sourgraves config`
  default: op
sourgraves.admin.settings:
  description: Allows you to use `/sourgraves settings`
  default: op
sourgraves.admin.give:
  description: Allows you use `/sourgraves give`
  default: op
sourgraves.player.graveaccess:
  description: A player with this permission can open any grave, regardless of whether it is public.
  default: false
sourgraves.player.permaprivate:
  description: A player with this permission will have a grave that never goes public.
  default: false
sourgraves.player.permagraves:
  description: A player with this permission will have a grave that never expires by itself. This doesn't affect other situations which delete a grave.
  default: false
sourgraves.utils.player:
  description: Allows you to use `/sourgraves player`
  default: op
sourgraves.utils.locateown:
  description: Allows you to use `/sourgraves locate`
  default: true
```