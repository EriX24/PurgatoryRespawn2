package com.de24.purgatoryrespawn2.entities;

import com.de24.purgatoryrespawn2.PurgatoryRespawn2;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Entities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PurgatoryRespawn2.MODID);

    public static final RegistryObject<EntityType<DeathSkullEntity>> DEATH_SKULL =
            ENTITY_TYPES.register("death_skull",
                    () -> EntityType.Builder.of(DeathSkullEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(32)
                            .updateInterval(20)
                            .build("death_skull"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
