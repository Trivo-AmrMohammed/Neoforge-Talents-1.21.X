package net.trivo.weaponsmod;

import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Weapon;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(WeaponsMod.MODID)
public class WeaponsMod {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "trivoweapons";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public WeaponsMod(IEventBus modEventBus, ModContainer modContainer) {

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onPlayerFirstLog(PlayerEvent.PlayerLoggedInEvent event) {
       if (event.getEntity() instanceof ServerPlayer player) {
           assignTalent(player);
       }
    }

    public void assignTalent(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("TalentAssigned")) {
            int chanceRoll = new Random().nextInt(5);

            if (chanceRoll == 1) {
                persistentData.putString("Talent", "combat");
                player.addItem(new ItemStack(Items.DIAMOND_SWORD));
            }
            else if (chanceRoll == 2) {
                persistentData.putString("Talent", "mining");
                player.addItem(new ItemStack(Items.DIAMOND_PICKAXE));
            }
            else if (chanceRoll == 3) {
                persistentData.putString("Talent", "foraging");
                player.addItem(new ItemStack(Items.DIAMOND_AXE));
            }

            player.addItem(new ItemStack(Items.DIAMOND));
            persistentData.putBoolean("TalentAssigned", true);
        }
    }

    public String getTalent(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains("Talent")) {
            return persistentData.getString("Talent").toString();
        }
        return "none";
    }

    @SubscribeEvent
    public void perPlayerTick(PlayerTickEvent.Post event) {
        LOGGER.info("ticking");
        if (event.getEntity() instanceof ServerPlayer player) {
            LOGGER.info("isserverplayer");
            if (!player.level().isClientSide) {
                LOGGER.info("isnt clientside");
                if (getTalent(player).equals("combat")) {
                    LOGGER.info("combat");
                    if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.SWORDS)) {
                        LOGGER.info("holding sword");
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1, 2));
                    }
                }
                if (getTalent(player).equals("mining")) {
                    if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.PICKAXES)) {
                        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 1, 2));
                    }
                }
                if (getTalent(player).equals("foraging")) {
                    if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.AXES)) {
                        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 1, 2));
                    }
                }
            }
        }
    }
}
