package net.trivo.weaponsmod.utilities;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.trivo.weaponsmod.utilities.TalentsUtilities.assignRandomTalent;

public class TalentsAdvancementUtilities {

    public static int getAdvancementsLeft(ServerPlayer serverPlayer) {
        ServerAdvancementManager advancementManager = serverPlayer.server.getAdvancements();
        Collection<AdvancementHolder> allHolders = advancementManager.getAllAdvancements();

        int total = 0;
        int completed = 0;

        for (AdvancementHolder holder : allHolders) {
            Advancement advancement = holder.value();

            if (advancement.display().isEmpty()) {
                continue;
            }

            total++;

            AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(holder);
            if (progress.isDone()) {
                completed++;
            }
        }

        LOGGER.info(String.valueOf(total - completed));
        return total - completed;
    }

    public static void advancementRandomTalentApplyChance(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        int advancementsLeft = TalentsAdvancementUtilities.getAdvancementsLeft(serverPlayer);
        Random random = new Random();

        if (advancementsLeft == 0) {
            Set<String> assigned = new HashSet<>();
            assignRandomTalent(serverPlayer, assigned);

        } else if (advancementsLeft > 0) {
            int chance = random.nextInt(advancementsLeft);
            if (chance == 0) {
                Set<String> assigned = new HashSet<>();
                assignRandomTalent(serverPlayer, assigned);
            }
        }
    }

    //add achievement tab for talents
}
