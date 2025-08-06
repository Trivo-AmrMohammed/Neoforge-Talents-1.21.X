package net.trivo.weaponsmod;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

@Mod(WeaponsMod.MODID)
public class WeaponsMod {
    public static final String MODID = "trivoweapons";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] TALENT_KEYS = {
            "talent_combat", "talent_mining", "talent_foraging", "talent_archery",
            "talent_farming", "talent_sneaking", "talent_swimming", "talent_running",
            "talent_jumping", "talent_endurance", "talent_riding", "talent_healthy"
    };

    public WeaponsMod(net.neoforged.bus.api.IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private boolean hasAnyTalent(CompoundTag persistentData) {
        for (String key : TALENT_KEYS) {
            if (persistentData.contains(key)) return true;
        }
        return false;
    }

    private void assignRandomTalent(ServerPlayer player, Set<String> assigned) {
        CompoundTag persistentData = player.getPersistentData();

        int ownedCount = 0;
        for (String talentKey : TALENT_KEYS) {
            if (persistentData.contains(talentKey)) {
                ownedCount++;
            }
        }

        if (ownedCount >= TALENT_KEYS.length) {
            LOGGER.info("Player {} already has all talents assigned.", player.getName().getString());
            return;
        }

        Random random = new Random();
        String talent;

        do {
            int x = random.nextInt(TALENT_KEYS.length);
            talent = TALENT_KEYS[x];
        } while (persistentData.contains(talent) || assigned.contains(talent));

        persistentData.putBoolean(talent, true);
        assigned.add(talent);
        LOGGER.info("Assigned talent {} to player {}", talent, player.getName().getString());
    }

    private void removeTalents(ServerPlayer serverPlayer) {
        CompoundTag persistentData = serverPlayer.getPersistentData();

        for (String key : TALENT_KEYS) {
            persistentData.remove(key);
        }
    }

    @SubscribeEvent
    public void onPlayerFirstLog(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag persistentData = player.getPersistentData();
        if (hasAnyTalent(persistentData)) return;

        Set<String> assigned = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            assignRandomTalent(player, assigned);
        }
    }

    public int getAdvancementsLeft(ServerPlayer player) {
        ServerAdvancementManager advancementManager = player.server.getAdvancements();

        Collection<AdvancementHolder> allHolders = advancementManager.getAllAdvancements();

        int total = 0;
        int completed = 0;

        for (AdvancementHolder holder : allHolders) {
            Advancement advancement = holder.value();

            if (advancement.display().isEmpty()) {
                continue;
            }

            total++;

            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            if (progress.isDone()) {
                completed++;
            }
        }

        LOGGER.info(String.valueOf(total - completed));
        return total - completed;
    }

    @SubscribeEvent
    public void advancementAccomplishedTalent(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            int advancementsLeft = getAdvancementsLeft(serverPlayer);
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
    }

    @SubscribeEvent
    public void applyTalentEffects(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        CompoundTag data = player.getPersistentData();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (data.contains("talent_healthy") && !player.getFoodData().needsFood())
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 5, 1, false, false));
        if (data.contains("talent_combat") && heldItem.is(ItemTags.SWORDS))
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 5, 1, false, false));
        if (data.contains("talent_mining") && (heldItem.is(ItemTags.PICKAXES) || heldItem.is(ItemTags.SHOVELS)))
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 5, 1, false, false));
        if (data.contains("talent_foraging") && (heldItem.is(ItemTags.AXES) || heldItem.is(ItemTags.HOES)))
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 5, 1, false, false));
        if (data.contains("talent_sneaking") && player.isCrouching())
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, false, false));
        if (data.contains("talent_swimming") && player.isInWater() && player.isSwimming())
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 5, 0, false, false));
        if (data.contains("talent_running") && player.isSprinting())
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 1, false, false));
        if (data.contains("talent_jumping") && player.isCrouching())
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 1, false, false));
        if (data.contains("talent_endurance")) {
            if (player.hasItemInSlot(EquipmentSlot.HEAD) && player.hasItemInSlot(EquipmentSlot.CHEST)
                    && player.hasItemInSlot(EquipmentSlot.LEGS) && player.hasItemInSlot(EquipmentSlot.FEET) && player.getOffhandItem().is(Items.SHIELD)) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 1, false, false));
            } else if (player.hasItemInSlot(EquipmentSlot.HEAD) || player.hasItemInSlot(EquipmentSlot.CHEST)
                    || player.hasItemInSlot(EquipmentSlot.LEGS) || player.hasItemInSlot(EquipmentSlot.FEET)) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 0, false, false));
            }
        }
    }

    @SubscribeEvent
    public void farmingTalent(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("talent_farming")) return;
        ServerLevel level = player.serverLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (state.getBlock() instanceof BeetrootBlock) {
            if (state.getValue(BlockStateProperties.AGE_3) == 3) {
                level.getServer().execute(() -> level.setBlockAndUpdate(pos, Blocks.BEETROOTS.defaultBlockState()
                        .setValue(BeetrootBlock.AGE, 2)));
            }
        } else if (state.getBlock() instanceof CropBlock crop) {
            if (crop.getAge(state) == crop.getMaxAge()) {
                int random = 1 + new Random().nextInt(3);
                level.getServer().execute(() -> level.setBlockAndUpdate(pos, crop.defaultBlockState()
                        .setValue(CropBlock.AGE, crop.getMaxAge() - random)));
            }
        } else if (state.getBlock() instanceof NetherWartBlock) {
            if (state.getValue(BlockStateProperties.AGE_3) == 3) {
                level.getServer().execute(() -> level.setBlockAndUpdate(pos, Blocks.NETHER_WART.defaultBlockState()
                        .setValue(NetherWartBlock.AGE, 2)));
            }
        } else if (state.getBlock() instanceof CocoaBlock) {
            if (state.getValue(BlockStateProperties.AGE_2) == 2) {
                level.getServer().execute(() -> level.setBlockAndUpdate(pos,
                        Blocks.COCOA.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                                state.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(CocoaBlock.AGE, 1)));
            }
        }
    }

    @SubscribeEvent
    public void archeryTalent(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getMainHandItem().is(Items.BOW)) {

            CompoundTag persistentData = player.getPersistentData();
            if (!persistentData.contains("talent_archery")) return;

            int charge = event.getCharge();
            event.setCharge(Math.min(charge * 2, 20));
        }
    }

    @SubscribeEvent
    public void RidingTalent(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof ServerPlayer serverPlayer)) return;
        if (!(event.getEntityBeingMounted() instanceof LivingEntity livingEntity)) return;

        MobEffectInstance speed = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1);
        MobEffectInstance jump = new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 1);

        CompoundTag persistentData = serverPlayer.getPersistentData();
        if (!persistentData.contains("talent_riding")) return;

        if (event.isMounting()) {
            livingEntity.addEffect(speed);
            livingEntity.addEffect(jump);
        }
        if (event.isDismounting()) {
            livingEntity.removeAllEffects();
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        CompoundTag oldData = oldPlayer.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        for (String key : TALENT_KEYS) {
            copyTalentIfPresent(oldData, newData, key);
        }
    }

    private void copyTalentIfPresent(CompoundTag oldData, CompoundTag newData, String key) {
        if (oldData.contains(key)) {
            newData.putBoolean(key, oldData.getBoolean(key));
        }
    }
}
