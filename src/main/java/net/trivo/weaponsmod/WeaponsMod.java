package net.trivo.weaponsmod;

import net.minecraft.client.color.item.Potion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.neoforged.neoforge.common.Tags.Items.POTIONS;

@Mod(WeaponsMod.MODID)
public class WeaponsMod {
    public static final String MODID = "trivoweapons";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] TALENT_KEYS = {
            "talent_combat", "talent_mining", "talent_foraging", "talent_archery",
            "talent_farming", "talent_sneaking", "talent_swimming", "talent_running",
            "talent_jumping", "talent_endurance"
    };

    public WeaponsMod(net.neoforged.bus.api.IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModPotions.register(modEventBus);
        ModEffects.register(modEventBus);
    }

    private boolean hasAnyTalent(CompoundTag persistentData) {
        for (String key : TALENT_KEYS) {
            if (persistentData.contains(key)) return true;
        }
        return false;
    }

    private void assignRandomTalent(ServerPlayer player, Set<String> assigned) {
        CompoundTag persistentData = player.getPersistentData();
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

    @SubscribeEvent
    public void applyTalentEffects(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        CompoundTag data = player.getPersistentData();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (data.contains("talent_combat") && heldItem.is(ItemTags.SWORDS))
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 5, 1, false, false));
        if (data.contains("talent_mining") && heldItem.is(ItemTags.PICKAXES) || heldItem.is(ItemTags.SHOVELS))
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 5, 1, false, false));
        if (data.contains("talent_foraging") && heldItem.is(ItemTags.AXES))
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 5, 1, false, false));
        if (data.contains("talent_sneaking") && player.isCrouching())
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, false, false));
        if (data.contains("talent_swimming") && player.isInWater() && player.isSwimming())
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 5, 0, false, false));
        if (data.contains("talent_running") && player.isSprinting())
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 5, 1, false, false));
        if (data.contains("talent_jumping") && player.isCrouching())
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 5, 1, false, false));
        if (data.contains("talent_endurance")
                || player.hasItemInSlot(EquipmentSlot.HEAD)
                || player.hasItemInSlot(EquipmentSlot.CHEST)
                || player.hasItemInSlot(EquipmentSlot.LEGS)
                || player.hasItemInSlot(EquipmentSlot.FEET))
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 5, 0, false, false));
    }

    @SubscribeEvent
    public void farmingTalent(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("talent_farming")) return;
        ServerLevel level = player.level();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (state.getBlock() instanceof CropBlock crop) {
            if (crop.getAge(state) == crop.getMaxAge()) {
                level.getServer().execute(() -> level.setBlockAndUpdate(pos, crop.defaultBlockState()));
            }
        } else if (state.getBlock() instanceof NetherWartBlock) {
            if (state.getValue(BlockStateProperties.AGE_3) == 3) {
                level.getServer().execute(() -> level.setBlockAndUpdate(pos, Blocks.NETHER_WART.defaultBlockState()));
            }
        } else if (state.getBlock() instanceof CocoaBlock) {
            if (state.getValue(BlockStateProperties.AGE_2) == 2) {
                level.getServer().execute(() -> level.setBlockAndUpdate(pos,
                        Blocks.COCOA.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                                state.getValue(BlockStateProperties.HORIZONTAL_FACING))));
            }
        }
    }

    @SubscribeEvent
    public void archeryTalent(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getMainHandItem().is(Items.BOW) || player.getMainHandItem().is(Items.CROSSBOW)) {

            CompoundTag persistentData = player.getPersistentData();
            if (!persistentData.contains("talent_archery")) return;

            int charge = event.getCharge();
            event.setCharge(Math.min(charge * 2, 20));
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        CompoundTag oldData = oldPlayer.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        copyTalentIfPresent(oldData, newData, "talent_combat");
        copyTalentIfPresent(oldData, newData, "talent_mining");
        copyTalentIfPresent(oldData, newData, "talent_foraging");
        copyTalentIfPresent(oldData, newData, "talent_archery");
        copyTalentIfPresent(oldData, newData, "talent_farming");
        copyTalentIfPresent(oldData, newData, "talent_sneaking");
        copyTalentIfPresent(oldData, newData, "talent_swimming");
        copyTalentIfPresent(oldData, newData, "talent_running");
        copyTalentIfPresent(oldData, newData, "talent_jumping");
        copyTalentIfPresent(oldData, newData, "talent_endurance");
        copyTalentIfPresent(oldData, newData, "talents_assigned");
    }

    private void copyTalentIfPresent(CompoundTag oldData, CompoundTag newData, String key) {
        if (oldData.contains(key)) {
            newData.putBoolean(key, oldData.getBoolean(key).orElse(false));
        }
    }
}
