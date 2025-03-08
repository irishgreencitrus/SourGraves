package io.github.irishgreencitrus.sourgraves

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.github.irishgreencitrus.brigadierdsl.argument
import io.github.irishgreencitrus.brigadierdsl.does
import io.github.irishgreencitrus.brigadierdsl.literal
import io.github.irishgreencitrus.brigadierdsl.rootLiteral
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.math.roundToInt

@Suppress("UnstableApiUsage")
object GraveCommand {
    private fun updateConfigAndSave(ctx: CommandContext<CommandSourceStack>) {
        ctx.source.sender.sendMessage(Component.text("Updated the configuration"))
        SourGraves.plugin.writeConfig(always = true)
    }

    fun createCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return rootLiteral("sourgraves") {
            literal("disk") {
                literal("save") {
                    does {
                        SourGraves.plugin.graveHandler.writeGravesFile(SourGraves.plugin.dataFolder)
                        it.source.sender.sendMessage(Component.text("Saved graves to disk!"))
                    }
                }
            }
            literal("configfile") {
                literal("reload") {
                    does {
                        SourGraves.plugin.loadConfig()
                        it.source.sender.sendMessage(Component.text("Loaded config from disk!"))
                    }
                }
            }
            literal("player") {
                argument("target", ArgumentTypes.player()) {
                    literal("count") {
                        does { ctx ->
                            val resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                            val player = resolver.resolve(ctx.source).first()
                            val ownedGraves = SourGraves.plugin.graveHandler.findOwnedGraves(player)
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
                                val ownedGraves = SourGraves.plugin.graveHandler.findOwnedGraves(player).toList()
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
                                val graveEntity = SourGraves.plugin.server.getEntity(graveData.linkedArmourStandUuid)
                                if (graveEntity == null) {
                                    ctx.source.sender.sendMessage(
                                        Component.text("Could not find grave location! Maybe the armour stand has been killed?")
                                            .color(NamedTextColor.RED)
                                    )
                                    return@does
                                }
                                val x = graveEntity.x.roundToInt()
                                val y = graveEntity.y.roundToInt() + 1
                                val z = graveEntity.z.roundToInt()

                                val msg = MiniMessage.miniMessage().deserialize(
                                    "<yellow>${player.name}</yellow>'s grave is at <hover:show_text:'Click to Teleport'><click:suggest_command:'/tp @s $x $y $z'><green>[$x, $y, $z]</green></click></hover>"
                                )

                                ctx.source.sender.sendMessage(msg)
                            }
                        }
                    }
                    literal("delete") {}
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
                    argument("number", IntegerArgumentType.integer(0, 1000)) {
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
                    argument("number", IntegerArgumentType.integer(1, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.publicInMinutes =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
                literal("grave_delete_in_minutes") {
                    argument("number", IntegerArgumentType.integer(1, 1000)) {
                        does { ctx ->
                            SourGraves.plugin.pluginConfig.deleteInMinutes =
                                IntegerArgumentType.getInteger(ctx, "number")
                            updateConfigAndSave(ctx)
                        }
                    }
                }
            }
        }
    }
}