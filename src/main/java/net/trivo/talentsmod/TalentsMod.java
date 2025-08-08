package net.trivo.talentsmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.trivo.talentsmod.commands.TalentsCommands;
import net.trivo.talentsmod.events.TalentsEventManager;
import net.trivo.talentsmod.talents.TalentsList;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

import static net.trivo.talentsmod.utilities.TalentsUtilities.*;

@Mod(TalentsMod.MODID)
public class TalentsMod {
    public static final String MODID = "trivotalents";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String FIRST_JOIN_TALENTS_KEY = "first_join_talents";

    public TalentsMod(net.neoforged.bus.api.IEventBus modEventBus, ModContainer modContainer) {
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
}
