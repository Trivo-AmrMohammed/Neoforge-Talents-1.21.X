package net.trivo.weaponsmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.trivo.weaponsmod.WeaponsMod.LOGGER;

public class RerollTalentsEffect extends InstantenousMobEffect {
    public RerollTalentsEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    private static final String[] TALENT_KEYS = {
            "talent_combat", "talent_mining", "talent_foraging", "talent_archery",
            "talent_farming", "talent_sneaking", "talent_swimming", "talent_running",
            "talent_jumping", "talent_endurance"
    };

    @Override
    public void applyInstantenousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity, int amplifier, double health) {
        if (entity instanceof ServerPlayer player) {
            CompoundTag persistentData = player.getPersistentData();
            Random random = new Random();

            for (String key : TALENT_KEYS) {
                persistentData.remove(key);
            }

            List<String> talentsPool = new ArrayList<>(List.of(TALENT_KEYS));
            Collections.shuffle(talentsPool, random);

            for (int i = 0; i < 3 && i < talentsPool.size(); i++) {
                String selectedTalent = talentsPool.get(i);
                persistentData.putBoolean(selectedTalent, true);
                LOGGER.info("Assigned talent {} to player {}", selectedTalent, player.getName().getString());
            }
        }
    }
}
