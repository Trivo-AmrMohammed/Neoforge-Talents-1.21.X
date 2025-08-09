package net.trivo.talentsmod.talents;

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

import java.util.*;

import static net.trivo.talentsmod.utilities.TalentsFunctionalityUtilities.applyTalent;

public class TalentsFunctionality {
        public static void applyTalentEffectsFunctionality(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;
        ItemStack heldItem = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);

        applyTalent(serverPlayer, TalentsList.Talents.HEALTHY, MobEffects.HEALTH_BOOST, 1, 30, !serverPlayer.getFoodData().needsFood());
        applyTalent(serverPlayer, TalentsList.Talents.FIGHTING, MobEffects.DAMAGE_BOOST, 0, 10, heldItem.is(ItemTags.SWORDS) || heldItem.is(ItemTags.AXES));
        applyTalent(serverPlayer, TalentsList.Talents.MINING, MobEffects.DIG_SPEED, 0, 10, heldItem.is(ItemTags.PICKAXES) || heldItem.is(ItemTags.SHOVELS));
        applyTalent(serverPlayer, TalentsList.Talents.FORAGING, MobEffects.DIG_SPEED, 0, 10, heldItem.is(ItemTags.AXES) || heldItem.is(ItemTags.HOES));
        applyTalent(serverPlayer, TalentsList.Talents.SNEAKING, MobEffects.INVISIBILITY, 2, serverPlayer.isCrouching());
        applyTalent(serverPlayer, TalentsList.Talents.SWIMMING, MobEffects.DOLPHINS_GRACE,  5, serverPlayer.isInWater() && serverPlayer.isSwimming());
        applyTalent(serverPlayer, TalentsList.Talents.RUNNING, MobEffects.MOVEMENT_SPEED, 0, 5, 5, serverPlayer.isSprinting());
        applyTalent(serverPlayer, TalentsList.Talents.JUMPING, MobEffects.JUMP, 0, 0, serverPlayer.isCrouching());
        applyTalent(serverPlayer, TalentsList.Talents.ENDURANCE, MobEffects.DAMAGE_RESISTANCE, 0, serverPlayer.hasItemInSlot(EquipmentSlot.HEAD) || serverPlayer.hasItemInSlot(EquipmentSlot.CHEST) || serverPlayer.hasItemInSlot(EquipmentSlot.LEGS) || serverPlayer.hasItemInSlot(EquipmentSlot.FEET), serverPlayer.hasItemInSlot(EquipmentSlot.HEAD) && serverPlayer.hasItemInSlot(EquipmentSlot.CHEST) && serverPlayer.hasItemInSlot(EquipmentSlot.LEGS) && serverPlayer.hasItemInSlot(EquipmentSlot.FEET) && serverPlayer.getOffhandItem().is(Items.SHIELD));
    }

    public static void farmingTalentFunctionality(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) return;
        if (!serverPlayer.getPersistentData().contains("talent_farming")) return;
        if (serverPlayer.serverLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        ServerLevel serverLevel = serverPlayer.serverLevel();

        if (state.getBlock() instanceof BeetrootBlock) {
            if (state.getValue(BlockStateProperties.AGE_3) == 3) {
                serverLevel.getServer().execute(() -> serverLevel.setBlockAndUpdate(pos, Blocks.BEETROOTS.defaultBlockState()
                        .setValue(BeetrootBlock.AGE, 2)));
            }
        } else if (state.getBlock() instanceof CropBlock crop) {
            if (crop.getAge(state) == crop.getMaxAge()) {
                int random = 1 + new Random().nextInt(3);
                serverLevel.getServer().execute(() -> serverLevel.setBlockAndUpdate(pos, crop.defaultBlockState()
                        .setValue(CropBlock.AGE, crop.getMaxAge() - random)));
            }
        } else if (state.getBlock() instanceof NetherWartBlock) {
            if (state.getValue(BlockStateProperties.AGE_3) == 3) {
                serverLevel.getServer().execute(() -> serverLevel.setBlockAndUpdate(pos, Blocks.NETHER_WART.defaultBlockState()
                        .setValue(NetherWartBlock.AGE, 2)));
            }
        } else if (state.getBlock() instanceof CocoaBlock) {
            if (state.getValue(BlockStateProperties.AGE_2) == 2) {
                serverLevel.getServer().execute(() -> serverLevel.setBlockAndUpdate(pos,
                        Blocks.COCOA.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                                state.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(CocoaBlock.AGE, 1)));
            }
        }
    }


    public static void archeryTalentFunctionality(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!serverPlayer.getPersistentData().contains("talent_archery")) return;

        if (serverPlayer.getMainHandItem().is(Items.BOW)) {
            int charge = event.getCharge();
            event.setCharge(Math.min(charge * 2, 20));
        }
    }

    public static void ridingTalentFunctionality(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof ServerPlayer serverPlayer)) return;
        if (!(event.getEntityBeingMounted() instanceof LivingEntity livingEntity)) return;
        if (!serverPlayer.getPersistentData().contains("talent_riding")) return;

        MobEffectInstance speed = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1, true, true, false);
        MobEffectInstance jump = new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 1, true, true, false);

        if (event.isMounting()) {
            livingEntity.addEffect(speed);
            livingEntity.addEffect(jump);
        }
        if (event.isDismounting()) {
            livingEntity.removeEffect(MobEffects.MOVEMENT_SPEED);
            livingEntity.removeEffect(MobEffects.JUMP);
        }
    }
}
