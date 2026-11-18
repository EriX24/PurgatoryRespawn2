package com.de24.purgatoryrespawn2.effect;

import com.de24.purgatoryrespawn2.PurgatoryRespawn2;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PurgatoryRespawn2.MODID);

    public static final RegistryObject<MobEffect> LIMBO = MOB_EFFECTS.register(
            "limbo", () -> new LimboEffect(MobEffectCategory.HARMFUL, 1));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}


