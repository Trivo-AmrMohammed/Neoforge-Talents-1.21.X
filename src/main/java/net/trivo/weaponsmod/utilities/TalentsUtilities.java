package net.trivo.weaponsmod.utilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.trivo.weaponsmod.talents.TalentsList;
import org.w3c.dom.stylesheets.LinkStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TalentsUtilities {

    public static boolean hasTalent(CompoundTag persistentData, TalentsList.Talents talent) {
        return persistentData.contains(talent.getKey());
    }

    public static boolean hasAnyTalent(CompoundTag persistentData) {
        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            if (persistentData.contains(talent.getKey()))
                return true;
        }
        return false;
    }

    public static boolean hasAllTalents(CompoundTag persistentData) {
        int talentsAmount = 0;
        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            if (persistentData.contains(talent.getKey()))
                talentsAmount++;
        }
        return talentsAmount >= TalentsList.Talents.values().length;
    }

    public static void assignTalent(ServerPlayer serverPlayer, TalentsList.Talents talent, Set<String> assigned) {
        if (!serverPlayer.getPersistentData().contains(talent.getKey())) {
            serverPlayer.getPersistentData().putBoolean(talent.getKey(), true);
            assigned.add(talent.getKey());
            System.out.println("Given " + serverPlayer.getName().getString() + " the talent " + talent.name() + ".");
        }
    }

    public static void removeTalent(ServerPlayer serverPlayer, TalentsList.Talents talent, Set<String> assigned) {
        if (serverPlayer.getPersistentData().contains(talent.getKey())) {
            serverPlayer.getPersistentData().remove(talent.getKey());
            assigned.remove(talent.getKey());
            System.out.println("Removed from " + serverPlayer.getName().getString() + " the talent " + talent.name() + ".");
        }
    }

    public static void assignRandomTalent(ServerPlayer serverPlayer, Set<String> assigned) {
        CompoundTag persistentData = serverPlayer.getPersistentData();

        int ownedCount = 0;
        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            if (persistentData.contains(talent.getKey())) {
                ownedCount++;
            }
        }

        if (ownedCount >= TalentsList.Talents.values().length) {
            LOGGER.info("Player {} already has all the talents.", serverPlayer.getName().getString());
            return;
        }

        Random random = new Random();
        String talent;

        do {
            int x = random.nextInt(TalentsList.Talents.values().length);
            talent = TalentsList.Talents.values()[x].getKey();
        } while (persistentData.contains(talent) || assigned.contains(talent));

        persistentData.putBoolean(talent, true);
        assigned.add(talent);
        LOGGER.info("{} was given the talent {}.", serverPlayer.getName().getString(), talent);
    }

    public static void removeRandomTalent(ServerPlayer serverPlayer, Set<String> assigned) {
        CompoundTag persistentData = serverPlayer.getPersistentData();

        List<String> ownedTalents = new ArrayList<>();
        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            if (persistentData.contains(talent.getKey())) {
                ownedTalents.add(talent.getKey());
            }
        }

        if (ownedTalents.isEmpty()) {
            LOGGER.info("{} doesn't have any talents.", serverPlayer.getName().getString());
            return;
        }

        Random random = new Random();
        String talent = ownedTalents.get(random.nextInt(ownedTalents.size()));

        persistentData.remove(talent);
        assigned.remove(talent);
        LOGGER.info("{} lost the talent {}.", serverPlayer.getName().getString(), talent);
    }
}
