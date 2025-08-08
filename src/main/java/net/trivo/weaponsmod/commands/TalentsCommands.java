package net.trivo.weaponsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.trivo.weaponsmod.talents.TalentsList;
import net.trivo.weaponsmod.utilities.TalentsUtilities;

import java.util.HashSet;

public class TalentsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("talents")
                .requires(source -> source.hasPermission(2));


        root.then(Commands.literal("assign")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("random")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    if (!TalentsUtilities.hasAllTalents(player.getPersistentData())) {
                                        TalentsUtilities.assignRandomTalent(player, new HashSet<>());
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Given " + player.getName().getString() + " a random talent."),
                                                false
                                        );
                                    } else {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(player.getName().getString() + " already has all the talents."),
                                                false
                                        );
                                    }
                                    return 1;
                                }))

                        .then(Commands.argument("talent", StringArgumentType.string())
                                .suggests(TALENT_SUGGESTIONS)
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    String talent = StringArgumentType.getString(context, "talent");
                                    if (!player.getPersistentData().contains(TalentsList.getKeyFromKeyword(talent))) {
                                        TalentsUtilities.assignTalent(player, TalentsList.Talents.valueOf(talent.toUpperCase()), new HashSet<>());
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Given " + player.getName().getString() + " the talent " + talent.toLowerCase() + "."),
                                                false
                                        );
                                    } else {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(player.getName().getString() + " already has the talent " + talent.toLowerCase() + "."),
                                                false
                                        );
                                    }
                                    return 1;
                                }))));


        root.then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("random")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    if (TalentsUtilities.hasAnyTalent(player.getPersistentData())) {
                                        TalentsUtilities.removeRandomTalent(player, new HashSet<>());
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Removed a random talent from " + player.getName().getString() + "."),
                                                false
                                        );
                                    } else {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(player.getName().getString() + " doesn't have any talents."),
                                                false
                                        );
                                    }
                                    return 1;
                                }))

                        .then(Commands.argument("talent", StringArgumentType.string())
                                .suggests(TALENT_SUGGESTIONS)
                                .executes(context -> {
                                    ServerPlayer serverPlayer = EntityArgument.getPlayer(context, "player");
                                    String talent = StringArgumentType.getString(context, "talent");
                                    if (serverPlayer.getPersistentData().contains(TalentsList.getKeyFromKeyword(talent))) {
                                        TalentsUtilities.removeTalent(serverPlayer, TalentsList.Talents.valueOf(talent.toUpperCase()), new HashSet<>());
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Removed the talent " + talent.toLowerCase() + " from " + serverPlayer.getName().getString() + "."),
                                                false
                                        );
                                    } else {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(serverPlayer.getName().getString() + " doesn't have the talent " + talent.toLowerCase() + "."),
                                                false
                                        );
                                    }
                                    return 1;
                                }))));


        root.then(Commands.literal("list")
            .then(Commands.argument("player", EntityArgument.player())
                .executes(context -> {
                    ServerPlayer serverPlayer = EntityArgument.getPlayer(context, "player");
                    if (TalentsUtilities.hasAnyTalent(serverPlayer.getPersistentData())) {
                        context.getSource().sendSuccess(
                                () -> Component.literal(serverPlayer.getName().getString() + " has the following talents:"),
                                false
                        );
                        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
                            if (serverPlayer.getPersistentData().contains(talent.getKey())) {
                                context.getSource().sendSuccess(
                                        () -> Component.literal(talent.toString()),
                                        false
                                );
                            }
                        }
                    } else {
                        context.getSource().sendSuccess(
                                () -> Component.literal(serverPlayer.getName().getString() + " has no talents."),
                                false
                        );
                    }
                    return 1;
                }))

                .executes(context -> {
                    ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
                    if (TalentsUtilities.hasAnyTalent(serverPlayer.getPersistentData())) {
                        context.getSource().sendSuccess(
                                () -> Component.literal("You have the following talents:"),
                                false
                        );
                        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
                            if (serverPlayer.getPersistentData().contains(talent.getKey())) {
                                context.getSource().sendSuccess(
                                        () -> Component.literal(talent.toString()),
                                        false
                                );
                            }
                        }
                    } else {
                        context.getSource().sendSuccess(
                                () -> Component.literal("You have no talents."),
                                false
                        );
                    }
                    return 1;
                })
        );

        dispatcher.register(root);
    }

    private static final SuggestionProvider<CommandSourceStack> TALENT_SUGGESTIONS = (context, builder) -> {
        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            builder.suggest(talent.toString().toLowerCase());
        }
        return builder.buildFuture();
    };
}
