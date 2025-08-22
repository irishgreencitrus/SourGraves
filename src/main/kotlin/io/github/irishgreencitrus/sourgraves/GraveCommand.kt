package io.github.irishgreencitrus.sourgraves

import com.mojang.brigadier.Command
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.irishgreencitrus.brigadierdsl.*
import io.github.irishgreencitrus.sourgraves.command.UUIDArgumentType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import kotlin.math.roundToInt

@Suppress("UnstableApiUsage")
object GraveCommand {
    private fun updateConfigAndSave(ctx: CommandContext<CommandSourceStack>) {
        ctx.source.sender.sendMessage(Component.text("Updated the configuration successfully"))
        SourGraves.plugin.writeConfig(always = true)
    }

    fun createCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return rootLiteral("sourgraves") {
            literal("disk") {
                literal("save") {
                    does {
                        SourGraves.storage.sync()
                        it.source.sender.sendMessage(Component.text("Saved graves to disk!"))
                    }
                }
                requires {
                    it.sender.hasPermission("sourgraves.admin.server")
                }
            }
            literal("reload") {
                does {
                    SourGraves.plugin.loadConfig()
                    it.source.sender.sendMessage(Component.text("Loaded config from disk!"))
                }
                requires {
                    it.sender.hasPermission("sourgraves.admin.server")
                }
            }
            literal("player") {
                argument("target", ArgumentTypes.player()) {
                    literal("count") {
                        does { ctx ->
                            val resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                            val player = resolver.resolve(ctx.source).first()
                            val ownedGraves = SourGraves.storage.searchPlayerGraves(player)
                            val ownedGravesPlural = if (ownedGraves.count() != 1) "s" else ""
                            ctx.source.sender.sendMessage(
                                Component.text(player.name).color(NamedTextColor.GREEN)
                                    .append(Component.text(" has ").color(NamedTextColor.WHITE))
                                    .append(
                                        Component.text("${ownedGraves.count()} grave${ownedGravesPlural}")
                                            .color(NamedTextColor.RED)
                                    )
                            )
                        }
                    }
                    literal("locate") {
                        argument("index", IntegerArgumentType.integer(0)) {
                            does { ctx ->
                                val index = IntegerArgumentType.getInteger(ctx, "index")
                                val resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                                val player = resolver.resolve(ctx.source).first()
                                val ownedGraves = SourGraves.storage.searchPlayerGraves(player).toList()
                                    .sortedBy { (_, second) -> second.createdAt }
                                if (ownedGraves.isEmpty()) {
                                    ctx.source.sender.sendMessage(
                                        Component.text("That player does not have any graves!")
                                            .color(NamedTextColor.RED)
                                    )
                                    return@does
                                }
                                if (index >= ownedGraves.count()) {
                                    ctx.source.sender.sendMessage(
                                        Component.text("Grave index out of range! Expected 0 to ${ownedGraves.count() - 1}")
                                            .color(NamedTextColor.RED)
                                    )
                                    return@does
                                }

                                val (_, graveData) = ownedGraves[index]
                                var graveLocation = graveData.cachedLocation
                                if (graveLocation.world == null) {
                                    val graveEntity =
                                        SourGraves.plugin.server.getEntity(graveData.linkedArmourStandUuid)
                                    if (graveEntity == null) {
                                        ctx.source.sender.sendMessage(
                                            Component.text("Could not find grave location! Maybe the armour stand has been killed?")
                                                .color(NamedTextColor.RED)
                                        )
                                        return@does
                                    }
                                    graveLocation = graveEntity.location
                                }
                                val x = graveLocation.x.roundToInt()
                                val y = graveLocation.y.roundToInt() + 1
                                val z = graveLocation.z.roundToInt()

                                val msg = MiniMessage.miniMessage().deserialize(
                                    "<yellow>${player.name}</yellow>'s grave is at <hover:show_text:'Click to Teleport'><click:suggest_command:'/tp @s $x $y $z'><green>[$x, $y, $z]</green></click></hover>"
                                )

                                ctx.source.sender.sendMessage(msg)
                            }
                        }
                    }
                }
                requires {
                    it.sender.hasPermission("sourgraves.utils.player")
                }
            }

            literal("undelete") {
                argument("uuid", UUIDArgumentType.uuid()) {
                    does { ctx ->
                        val uuid = UUIDArgumentType.getUuid("uuid", ctx)
                        if (!SourGraves.storage.supportsSoftDelete()) {
                            throw SimpleCommandExceptionType(LiteralMessage("Soft Deletion is not supported by your storage")).create()
                        }
                        val worked = SourGraves.storage.undelete(uuid)
                        if (!worked) {
                            if (!SourGraves.plugin.pluginConfig.sql.softDeletion)
                                throw SimpleCommandExceptionType(LiteralMessage("Soft deletion is disabled, the grave cannot be recovered.")).create()
                            throw SimpleCommandExceptionType(LiteralMessage("The grave UUID was not found")).create()
                        }

                        ctx.source.sender.sendMessage(Component.text("Successfully undeleted grave with uuid $uuid"))
                    }
                }
            }
            literal("give") {
                literal("by_grave_uuid") {
                    argument("uuid", UUIDArgumentType.uuid()) {
                        argument("give_to", ArgumentTypes.player()) {
                            does { ctx ->
                                val resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                val player = resolver.resolve(ctx.source).first()
                                val uuid = UUIDArgumentType.getUuid("uuid", ctx)
                                val graveData = SourGraves.storage[uuid]
                                    ?: throw SimpleCommandExceptionType(LiteralMessage("That UUID isn't a valid grave")).create()

                                val leftovers =
                                    player.inventory.addItem(*graveData.items.filterNotNull().toTypedArray())
                                leftovers.values.forEach {
                                    player.world.dropItemNaturally(player.location, it)
                                }

                                SourGraves.plugin.graveHandler.deleteGraveFromWorld(uuid, canDropItems = false)
                                ctx.source.sender.sendMessage(Component.text("Given contents to ${player.name}. The grave has been deleted."))
                            }
                        }

                    }
                }
                literal("oldest") {
                    argument("grave_owner", ArgumentTypes.player()) {
                        argument("give_to", ArgumentTypes.player()) {
                            does { ctx ->
                                val ownerResolver =
                                    ctx.getArgument("grave_owner", PlayerSelectorArgumentResolver::class.java)
                                val graveOwner = ownerResolver.resolve(ctx.source).first()

                                val recvResolver =
                                    ctx.getArgument("give_to", PlayerSelectorArgumentResolver::class.java)
                                val recvPlayer = recvResolver.resolve(ctx.source).first()

                                val (uuid, oldestGrave) = SourGraves.storage.oldestGrave(graveOwner)
                                    ?: throw SimpleCommandExceptionType(LiteralMessage("Couldn't find oldest grave for player ${graveOwner.name}")).create()

                                val leftovers =
                                    recvPlayer.inventory.addItem(*oldestGrave.items.filterNotNull().toTypedArray())
                                leftovers.values.forEach {
                                    recvPlayer.world.dropItemNaturally(recvPlayer.location, it)
                                }

                                SourGraves.plugin.graveHandler.deleteGraveFromWorld(uuid, canDropItems = false)
                                ctx.source.sender.sendMessage(Component.text("Given ${graveOwner.name}'s oldest grave to ${recvPlayer.name}. The grave has been deleted."))
                            }
                        }
                    }
                }
                literal("newest") {
                    argument("grave_owner", ArgumentTypes.player()) {
                        argument("give_to", ArgumentTypes.player()) {
                            does { ctx ->
                                val ownerResolver =
                                    ctx.getArgument("grave_owner", PlayerSelectorArgumentResolver::class.java)
                                val graveOwner = ownerResolver.resolve(ctx.source).first()

                                val recvResolver =
                                    ctx.getArgument("give_to", PlayerSelectorArgumentResolver::class.java)
                                val recvPlayer = recvResolver.resolve(ctx.source).first()

                                val (uuid, newestGrave) = SourGraves.storage.newestGrave(graveOwner)
                                    ?: throw SimpleCommandExceptionType(LiteralMessage("Couldn't find newest grave for player ${graveOwner.name}")).create()

                                val leftovers =
                                    recvPlayer.inventory.addItem(*newestGrave.items.filterNotNull().toTypedArray())
                                leftovers.values.forEach {
                                    recvPlayer.world.dropItemNaturally(recvPlayer.location, it)
                                }

                                SourGraves.plugin.graveHandler.deleteGraveFromWorld(uuid, canDropItems = false)
                                ctx.source.sender.sendMessage(Component.text("Given ${graveOwner.name}'s newest grave to ${recvPlayer.name}. The grave has been deleted."))
                            }
                        }
                    }
                }
                requires {
                    it.sender.hasPermission("sourgraves.admin.give")
                }
            }
            literal("settings") {
                literal("recover_particle") {
                    argument("type", ArgumentTypes.resourceKey(RegistryKey.PARTICLE_TYPE)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.recoverParticle =
                                RegistryArgumentExtractor.getTypedKey(ctx, RegistryKey.PARTICLE_TYPE, "type").key()
                                    .value()
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("recover_particle_amount") {
                    argument("number", IntegerArgumentType.integer(0)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.recoverParticleAmount =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("recover_sound") {
                    argument("sound", ArgumentTypes.resourceKey(RegistryKey.SOUND_EVENT)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.recoverSound =
                                RegistryArgumentExtractor.getTypedKey(ctx, RegistryKey.SOUND_EVENT, "sound").key()
                                    .toString()
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("max_graves_per_player") {
                    argument("number", IntegerArgumentType.integer(-1, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.maxGravesPerPlayer =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("drop_items_on_too_many_graves") {
                    argument("value", BoolArgumentType.bool()) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.dropItemsOnTooManyGraves =
                                BoolArgumentType.getBool(ctx, "value")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("drop_items_on_grave_deletion") {
                    argument("value", BoolArgumentType.bool()) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.dropItemsOnGraveDeletion =
                                BoolArgumentType.getBool(ctx, "value")
                            updateConfigAndSave(ctx)
                        }

                    }
                }
                literal("cleanup_delay_minutes") {
                    argument("number", IntegerArgumentType.integer(0, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.periodicCleanupDelayMinutes =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }

                    }
                }
                literal("cleanup_period_minutes") {
                    argument("number", IntegerArgumentType.integer(1, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.periodicCleanupPeriodMinutes =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }

                    }
                }
                literal("grave_public_in_minutes") {
                    argument("number", IntegerArgumentType.integer(-1, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.publicInMinutes =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("grave_delete_in_minutes") {
                    argument("number", IntegerArgumentType.integer(-1, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.deleteInMinutes =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("notify_coords_on_respawn") {
                    argument("value", BoolArgumentType.bool()) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.notifyCoordsOnRespawn =
                                BoolArgumentType.getBool(ctx, "value")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("log_cleanup_task_runs") {
                    argument("value", BoolArgumentType.bool()) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.logMessages.cleanupTask =
                                BoolArgumentType.getBool(ctx, "value")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                requires {
                    it.sender.hasPermission("sourgraves.admin.settings")
                }
            }
            literal("locate") {
                literal("nearest") {
                    doesReturning { ctx ->
                        val player = ctx.source.executor as Player
                        val graves = SourGraves.storage.searchPlayerGraves(player).toList()
                        if (graves.isEmpty()) {
                            player.sendMessage(Component.text("You don't have any graves!").color(NamedTextColor.RED))
                            return@doesReturning 2
                        }

                        val gravesWithDistance = graves.map { g ->
                            val entity = player.server.getEntity(g.second.linkedArmourStandUuid)
                            Triple(g.first, g.second, entity?.location ?: g.second.cachedLocation)
                        }.filter { t ->
                            t.third.world?.uid == player.world.uid
                        }.sortedBy { t ->
                            val distance = t.third.distanceSquared(player.location)
                            distance
                        }

                        if (gravesWithDistance.isEmpty()) {
                            player.sendMessage(
                                Component.text("You don't have any graves in this dimension!").color(NamedTextColor.RED)
                            )
                            return@doesReturning 2
                        }

                        val target = gravesWithDistance.first().third

                        player.sendMessage(Component.text("Your nearest grave is at ${target.blockX} ${target.blockY} ${target.blockZ}"))
                        Command.SINGLE_SUCCESS
                    }
                }
                literal("furthest") {
                    doesReturning { ctx ->
                        val player = ctx.source.executor as Player
                        val graves = SourGraves.storage.searchPlayerGraves(player).toList()
                        if (graves.isEmpty()) {
                            player.sendMessage(Component.text("You don't have any graves!").color(NamedTextColor.RED))
                            return@doesReturning 2
                        }

                        val gravesWithDistance = graves.map { g ->
                            val entity = player.server.getEntity(g.second.linkedArmourStandUuid)
                            Triple(g.first, g.second, entity?.location ?: g.second.cachedLocation)
                        }.filter { t ->
                            t.third.world?.uid == player.world.uid
                        }.sortedByDescending { t ->
                            val distance = t.third.distanceSquared(player.location)
                            distance
                        }

                        if (gravesWithDistance.isEmpty()) {
                            player.sendMessage(
                                Component.text("You don't have any graves in this dimension!").color(NamedTextColor.RED)
                            )
                            return@doesReturning 2
                        }

                        val target = gravesWithDistance.first().third
                        player.sendMessage(Component.text("Your furthest grave is at ${target.blockX} ${target.blockY} ${target.blockZ}"))
                        Command.SINGLE_SUCCESS
                    }
                }
                literal("oldest") {
                    doesReturning { ctx ->
                        val player = ctx.source.executor as Player
                        val hasGraves = SourGraves.storage.searchPlayerGraves(player).isNotEmpty()
                        if (!hasGraves) {
                            player.sendMessage(Component.text("You don't have any graves!").color(NamedTextColor.RED))
                            return@doesReturning 2
                        }
                        val grave = SourGraves.storage.oldestGrave(player, dimension = player.world.name)
                        if (grave == null) {
                            player.sendMessage(
                                Component.text("You don't have any graves in this dimension!").color(NamedTextColor.RED)
                            )
                            return@doesReturning 2
                        }
                        val target = GraveHelper.getArmourStandLocation(player.server, grave.second)
                        if (target == null) {
                            player.sendMessage(
                                Component.text("The grave found was invalid, maybe the armour stand has been killed?")
                                    .color(NamedTextColor.RED)
                            )
                            return@doesReturning 2
                        }
                        player.sendMessage(Component.text("Your oldest grave is at ${target.blockX} ${target.blockY} ${target.blockZ}"))
                        Command.SINGLE_SUCCESS
                    }
                }
                literal("newest") {
                    doesReturning { ctx ->
                        val player = ctx.source.executor as Player
                        val hasGraves = SourGraves.storage.searchPlayerGraves(player).isNotEmpty()
                        if (!hasGraves) {
                            player.sendMessage(Component.text("You don't have any graves!").color(NamedTextColor.RED))
                            return@doesReturning 2
                        }
                        val grave = SourGraves.storage.newestGrave(player, dimension = player.world.name)
                        if (grave == null) {
                            player.sendMessage(
                                Component.text("You don't have any graves in this dimension!").color(NamedTextColor.RED)
                            )
                            return@doesReturning 2
                        }
                        val target = GraveHelper.getArmourStandLocation(player.server, grave.second)
                        if (target == null) {
                            player.sendMessage(
                                Component.text("The grave found was invalid, maybe the armour stand has been killed?")
                                    .color(NamedTextColor.RED)
                            )
                            return@doesReturning 2
                        }

                        player.sendMessage(Component.text("Your newest grave is at ${target.blockX} ${target.blockY} ${target.blockZ}"))
                        Command.SINGLE_SUCCESS
                    }
                }
                requires {
                    it.sender.hasPermission("sourgraves.utils.locateown")
                            && it.sender is Player
                }
            }
        }
    }
}