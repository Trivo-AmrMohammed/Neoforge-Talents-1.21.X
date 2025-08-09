package net.trivo.talentsmod.utilities;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.trivo.talentsmod.talents.TalentsList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TalentsFunctionalityUtilities {
    public record PlayerTalentKey(UUID playerUUID, String talentKey) {}
    private static final Map<PlayerTalentKey, Integer> playerTick = new HashMap<>();

    public static void incrementTickCounter(UUID playerUUID, String talentKey) {
        PlayerTalentKey key = new PlayerTalentKey(playerUUID, talentKey);
        int current = playerTick.getOrDefault(key, 0);
        playerTick.put(key, current + 1);
    }

    public static void resetTickCounter(UUID playerUUID, String talentKey) {
        PlayerTalentKey key = new PlayerTalentKey(playerUUID, talentKey);
        playerTick.put(key, 0);
    }

    public static int getTickCounter(UUID playerUUID, String talentKey) {
        PlayerTalentKey key = new PlayerTalentKey(playerUUID, talentKey);
        return playerTick.getOrDefault(key, 0);
    }

    public static void applyTalent(ServerPlayer serverPlayer, TalentsList.Talents talent, Holder<MobEffect> mobEffect, int startingAmplifier, int waitSeconds, boolean requirement) {
        if (!(serverPlayer.getPersistentData().contains(talent.getKey()))) return;

        if (requirement) {
            incrementTickCounter(serverPlayer.getUUID(), talent.getKey());
            if (getTickCounter(serverPlayer.getUUID(), talent.getKey()) >= (waitSeconds * 20))
                serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, startingAmplifier + 1, true, true, false));
            else
                serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, startingAmplifier, true, true, false));
        }
        else resetTickCounter(serverPlayer.getUUID(), talent.getKey());
    }

    public static void applyTalent(ServerPlayer serverPlayer, TalentsList.Talents talent, Holder<MobEffect> mobEffect, int waitSeconds, boolean requirement) {
        if (!(serverPlayer.getPersistentData().contains(talent.getKey()))) return;

        if (requirement) {
            incrementTickCounter(serverPlayer.getUUID(), talent.getKey());
            if (getTickCounter(serverPlayer.getUUID(), talent.getKey()) >= (waitSeconds * 20)) {
                serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, 0, true, true, false));
            }
        }
        else resetTickCounter(serverPlayer.getUUID(), talent.getKey());
    }

    public static void applyTalent(ServerPlayer serverPlayer, TalentsList.Talents talent, Holder<MobEffect> mobEffect, int startingAmplifier, int waitSecondsFirst, int waitSecondsSecond, boolean requirement) {
        if (!(serverPlayer.getPersistentData().contains(talent.getKey()))) return;

        if (requirement) {
            incrementTickCounter(serverPlayer.getUUID(), talent.getKey());
            if (getTickCounter(serverPlayer.getUUID(), talent.getKey()) >= (waitSecondsFirst * 20)) {
                serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, startingAmplifier, true, true, false));
                if (getTickCounter(serverPlayer.getUUID(), talent.getKey()) >= ((waitSecondsFirst + waitSecondsSecond) * 20)) {
                    serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, startingAmplifier + 1, true, true, false));
                }
            }
        }
        else resetTickCounter(serverPlayer.getUUID(), talent.getKey());
    }

    public static void applyTalent(ServerPlayer serverPlayer, TalentsList.Talents talent, Holder<MobEffect> mobEffect, int startingAmplifier, boolean requirementOne, boolean requirementTwo) {
        if (!(serverPlayer.getPersistentData().contains(talent.getKey()))) return;

        if (requirementOne) serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, startingAmplifier, true, true, false));
        if (requirementTwo) serverPlayer.addEffect(new MobEffectInstance(mobEffect, 10, startingAmplifier + 1, true, true, false));
    }
}
