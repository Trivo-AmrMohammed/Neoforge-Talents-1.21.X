package net.trivo.weaponsmod.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.trivo.weaponsmod.commands.TalentsClientCommands;
import net.trivo.weaponsmod.talents.TalentsFunctionality;

import static net.trivo.weaponsmod.talents.TalentsFunctionality.*;
import static net.trivo.weaponsmod.utilities.TalentsAdvancementUtilities.advancementRandomTalentApplyChance;

public class TalentsEventManager {

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        TalentsClientCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void applyTalentEffects(PlayerTickEvent.Post event) {
        TalentsFunctionality.applyTalentEffectsFunctionality(event);
    }

    @SubscribeEvent
    public void advancementAccomplishedTalent(AdvancementEvent.AdvancementEarnEvent event) {
        advancementRandomTalentApplyChance(event);
    }

    @SubscribeEvent
    public void farmingTalent(BlockEvent.BreakEvent event) {
        farmingTalentFunctionality(event);
    }

    @SubscribeEvent
    public void archeryTalent(ArrowLooseEvent event) {
        archeryTalentFunctionality(event);
    }

    @SubscribeEvent
    public void ridingTalent(EntityMountEvent event) {
        ridingTalentFunctionality(event);
    }
}
