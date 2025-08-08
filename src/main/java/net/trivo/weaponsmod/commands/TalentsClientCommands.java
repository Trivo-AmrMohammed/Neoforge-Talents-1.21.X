package net.trivo.weaponsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.trivo.weaponsmod.talents.TalentsList;

public class TalentsClientCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("talents")
                .executes( context -> {
                        context.getSource().sendSystemMessage(Component.literal("The current existing talents are:"));
                        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
                            context.getSource().sendSystemMessage(Component.literal(talent.toString()));
                        }
                    return 1;
                })
        );
    }
}
