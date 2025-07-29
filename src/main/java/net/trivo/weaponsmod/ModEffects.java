package net.trivo.weaponsmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.Holder;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, WeaponsMod.MODID);

    public static final Holder<MobEffect> REROLL_TALENTS_EFFECT =
            MOB_EFFECTS.register("reroll_talents_effect", () -> new RerollTalentsEffect(
                    MobEffectCategory.BENEFICIAL,
                    0xffffff
            ));

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
