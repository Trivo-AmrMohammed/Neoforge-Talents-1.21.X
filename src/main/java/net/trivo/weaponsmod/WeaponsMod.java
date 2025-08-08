package net.trivo.weaponsmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.trivo.weaponsmod.commands.TalentsCommands;
import net.trivo.weaponsmod.events.TalentsEventManager;
import net.trivo.weaponsmod.talents.TalentsList;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

import static net.trivo.weaponsmod.utilities.TalentsUtilities.*;

@Mod(WeaponsMod.MODID)
public class WeaponsMod {
    public static final String MODID = "trivoweapons";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String FIRST_JOIN_TALENTS_KEY = "first_join_talents";

    public WeaponsMod(net.neoforged.bus.api.IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(new TalentsEventManager());
    }

    @SubscribeEvent
    public void onPlayerFirstLog(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag persistentData = player.getPersistentData();

        if (!(persistentData.contains(FIRST_JOIN_TALENTS_KEY))) {
            for (int i = 0; i < 3; i++) {
                assignRandomTalent(player, new HashSet<>());
            }
            persistentData.putBoolean(FIRST_JOIN_TALENTS_KEY, true);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        CompoundTag oldData = oldPlayer.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        for (TalentsList.Talents talent : TalentsList.Talents.values()) {
            copyTalentIfPresent(oldData, newData, talent.getKey());
        }

        copyTalentIfPresent(oldData, newData, FIRST_JOIN_TALENTS_KEY);
    }

    private void copyTalentIfPresent(CompoundTag oldData, CompoundTag newData, String key) {
        if (oldData.contains(key)) {
            newData.putBoolean(key, oldData.getBoolean(key));
        }
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        TalentsCommands.register(event.getDispatcher());
    }
}
