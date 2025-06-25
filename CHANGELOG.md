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