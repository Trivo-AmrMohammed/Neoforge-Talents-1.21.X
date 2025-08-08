package net.trivo.weaponsmod.talents;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.trivo.weaponsmod.utilities.TalentsUtilities;

import java.util.Random;

public class TalentsFunctionality {

    public static void applyTalent(ServerPlayer serverPlayer, TalentsList.Talents talents, boolean requirement, MobEffectInstance mobEffect) {
        if (serverPlayer.getPersistentData().contains(talents.getKey()) && requirement) {
            serverPlayer.addEffect(mobEffect);
        }
    }

    public static void applyTalentEffectsFunctionality(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

        ItemStack heldItem = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);

        applyTalent(serverPlayer, TalentsList.Talents.HEALTHY, !serverPlayer.getFoodData().needsFood(),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 5, 1, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.FIGHTING, heldItem.is(ItemTags.SWORDS),
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 5, 1, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.MINING, heldItem.is(ItemTags.PICKAXES) || heldItem.is(ItemTags.SHOVELS),
                new MobEffectInstance(MobEffects.DIG_SPEED, 5, 1, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.FORAGING, heldItem.is(ItemTags.AXES) || heldItem.is(ItemTags.HOES),
                new MobEffectInstance(MobEffects.DIG_SPEED, 5, 1, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.SNEAKING, serverPlayer.isCrouching(),
                new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.SWIMMING, serverPlayer.isInWater() && serverPlayer.isSwimming(),
                new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 5, 0, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.RUNNING, serverPlayer.isSprinting(),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 1, true, false));

        applyTalent(serverPlayer, TalentsList.Talents.JUMPING, serverPlayer.isCrouching(),
                new MobEffectInstance(MobEffects.JUMP, 15, 1, true, false));

        if (serverPlayer.hasItemInSlot(EquipmentSlot.HEAD) && serverPlayer.hasItemInSlot(EquipmentSlot.CHEST) && serverPlayer.hasItemInSlot(EquipmentSlot.LEGS) && serverPlayer.hasItemInSlot(EquipmentSlot.FEET) && serverPlayer.getOffhandItem().is(Items.SHIELD))
        applyTalent(serverPlayer, TalentsList.Talents.ENDURANCE, true,
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 1, true, false));
        else
            applyTalent(serverPlayer, TalentsList.Talents.ENDURANCE, serverPlayer.hasItemInSlot(EquipmentSlot.HEAD) || serverPlayer.hasItemInSlot(EquipmentSlot.CHEST)
                    || serverPlayer.hasItemInSlot(EquipmentSlot.LEGS) || serverPlayer.hasItemInSlot(EquipmentSlot.FEET),
                    new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 0, true, false));
    }

    public static void farmingTalentFunctionality(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) return;

        CompoundTag persistentData = serverPlayer.getPersistentData();
        if (!persistentData.contains("talent_farming")) return;

        ServerLevel level = serverPlayer.serverLevel();
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


    public static void archeryTalentFunctionality(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getMainHandItem().is(Items.BOW)) {

            CompoundTag persistentData = player.getPersistentData();
            if (!persistentData.contains("talent_archery")) return;

            int charge = event.getCharge();
            event.setCharge(Math.min(charge * 2, 20));
        }
    }

    public static void ridingTalentFunctionality(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof ServerPlayer serverPlayer)) return;
        if (!(event.getEntityBeingMounted() instanceof LivingEntity livingEntity)) return;

        MobEffectInstance speed = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1, true, false);
        MobEffectInstance jump = new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 1, true, false);

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
}
