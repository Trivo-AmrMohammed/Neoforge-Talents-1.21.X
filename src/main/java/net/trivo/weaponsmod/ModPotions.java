package net.trivo.weaponsmod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, WeaponsMod.MODID);

    public static final Holder<Potion> REWRITE_GENES_POTION = POTIONS.register
            ("rewrite_genes_potion", registryName ->
                    new Potion(registryName.getPath(),
            new MobEffectInstance(ModEffects.REROLL_TALENTS_EFFECT, 0, 0, false, false)
    ));
    public static void register(IEventBus eventBus){
        POTIONS.register(eventBus);
    }

}

