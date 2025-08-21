# Release 2.3.0

Bug fixes related to grave restoration, especially chest access:

Fix empty graves not being deleted Fix graves sometimes dropping barrier blocks Fix graves replacing filled slots with
empty on restore

# Release 2.2.0

Move at what point during death grave storage happens. It used to happen at `EventPriority.LOWEST` and this has been
moved to `EventPriority.HIGH`.
This allows other plugins to modify the inventory before a Grave is created.

# Release 2.1.1

Fix bug where the plugin would crash if Vault was installed in place of VaultUnlocked.

# Release 2.1.0

Add bStats metrics.

https://bstats.org/plugin/bukkit/SourGraves/26681

# Release 2.0.0

## SQL support

- Change grave storage format from JSON to SQLite.
- Add automated conversion from legacy storage format to the new SQL based one.
- Also add support for the Postgres and MySQL servers.
- Support soft deletion for the SQL servers so that data is never lost, only marked as deleted.

Legacy JSON storage can be re-enabled in the config.
The plugin will disable itself to prevent data loss if the configuration is incorrect.

## Timers based on game time

Grave expiry is now based on game time rather than the system clock.
This means graves should be work better for servers that shutdown frequently.

## Improved log messages

- Log messages can now be configured
- Log messages are generally less frequent and less annoying
- Severe log messages now appear for a reason.
- The infamous message for graves which existed before `v1.4.0` only appear once per startup.

## More Permissions

- A player with `sourgraves.player.permaprivate` will never have their grave accessible to others.
- A player with `sourgraves.player.permagraves` will never have their grave expired and killed by the server, but it
  could still be killed by other means such as too many graves or the grave give command.

## More stable inventory saving

The plugin now respects other plugins that change `keepInventory` during death. This is a thing you can do (e.g. custom
totems), so the plugin now respects it. This prevents item duplication.

## Grave `give` command

There is a new command available which is `/sourgraves give [options]`.

At the moment there are 3 versions of the command:
> Note: After this command is executed, the grave will be deleted from
> storage to prevent duplication of items.

### `by_grave_uuid`

```
/sourgraves give by_grave_uuid <uuid> <give_to>
```

Give the grave found at `uuid` to the player `give_to`

- The `uuid` can be found by looking through the database.
- `give_to` is the player to give the contents of the grave to.

### `oldest`

```
/sourgraves give oldest <grave_owner> <give_to>
```

Give the oldest grave owned by `grave_owner` to `give_to`.

- `grave_owner` is the player that owns the grave.
- `give_to` is the player to give the contents of the grave to.

### `newest`

```
/sourgraves give newest <grave_owner> <give_to>
```

Give the newest grave owned by `grave_owner` to `give_to`.

- `grave_owner` is the player that owns the grave.
- `give_to` is the player to give the contents of the grave to.

## Chest-like Grave Access

Enabling `allowChestLikeGraveAccess` will mean that clicking on a grave will
cause it to open an inventory instead of restoring it directly.

Shift-clicking will restore the grave as normal.

The click and shift-click behaviour can be swapped using `chestAccessSwapNormalAndShift`

## Other Config Options

- `disableForPvpKills` will mean that graves will not spawn for PvP kills, and the default Minecraft behaviour will
  occur.
- `forceLegacyFileStorage` for re-enabling JSON storage.
- `disabledWorlds` for disabling graves in a specific world.

# Release 1.5.0

- Add basic economy support
- Cache locations of unloaded graves, so locate command works properly on any grave
- Add graveaccess permission
- Remove extraneous messages relating to graves being valid

# Release 1.4.0

Add an option to disable the `[SourGraves] Cleaned graves and written to disk` message.
This is in the config as `logCleanupTaskRuns` and is now `false` by default.

# Release 1.3.0

- Fix bug related to Bukkit not properly reporting the version of the plugin.
- Fix items always being dropped even with the purge graves config being `false`.
- Add some notes into the config file.

# Release 1.2.0

- Fix a bug related to permissions not working correctly which meant anyone could edit settings (oops)
- Add `/sourgraves locate` which allows a player to locate their oldest, newest, nearest or furthest grave.
- Add a setting to notify the player of their grave's coordinates when they respawn.
- Make the permissions more granular