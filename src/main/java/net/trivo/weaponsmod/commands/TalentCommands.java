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

public class TalentCommands {
    private static final SuggestionProvider<CommandSourceStack> TALENT_SUGGESTIONS = (context, builder) -> {
        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            builder.suggest(talent.toString().toLowerCase());
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("talents")
                .requires(source -> source.hasPermission(2));


        root.then(Commands.literal("assign")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("random")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    if (!(TalentsUtilities.hasAllTalents(player.getPersistentData()))) {
                                        TalentsUtilities.assignRandomTalent(player, new HashSet<>());
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " got a random talent."));
                                    } else
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " already has all the talents."));
                                    return 1;
                                }))

                        .then(Commands.argument("talent", StringArgumentType.string())
                                .suggests(TALENT_SUGGESTIONS)
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    String talent = StringArgumentType.getString(context, "talent");
                                    if (!(player.getPersistentData().contains(TalentsList.getKeyFromKeyword(talent)))) {
                                        TalentsUtilities.assignTalent(player, TalentsList.Talents.valueOf(talent.toUpperCase()), new HashSet<>());
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " got the talent " + talent.toLowerCase() + "."));
                                    } else
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " already has the talent " + talent.toLowerCase() + "."));
                                    return 1;
                                }))));


        root.then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("random")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    if (TalentsUtilities.hasAnyTalent(player.getPersistentData())) {
                                        TalentsUtilities.removeRandomTalent(player, new HashSet<>());
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " lost a random talent."));
                                    } else
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " doesn't have any talents."));
                                    return 1;
                                }))

                        .then(Commands.argument("talent", StringArgumentType.string())
                                .suggests(TALENT_SUGGESTIONS)
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    String talent = StringArgumentType.getString(context, "talent");
                                    if (player.getPersistentData().contains(TalentsList.getKeyFromKeyword(talent))) {
                                        TalentsUtilities.removeTalent(player, TalentsList.Talents.valueOf(talent.toUpperCase()), new HashSet<>());
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " lost the talent " + talent.toLowerCase() + "."));
                                    } else
                                        player.sendSystemMessage(Component.literal(player.getName().getString() + " doesn't have the talent " + talent.toLowerCase() + "."));
                                    return 1;
                                }))));


        root.then(Commands.literal("list")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (TalentsUtilities.hasAnyTalent(player.getPersistentData())) {
                                player.sendSystemMessage(Component.literal(player.getName().getString() + " has the following talents:"));
                                for (TalentsList.Talents talent : TalentsList.Talents.values()) {
                                    if (player.getPersistentData().contains(talent.getKey())) {
                                        player.sendSystemMessage(Component.literal(talent.toString()));
                                    }
                                }
                            } else
                                player.sendSystemMessage(Component.literal(player.getName().getString() + " has no talents."));

                            return 1;
                        })));

        dispatcher.register(root);
    }
}
