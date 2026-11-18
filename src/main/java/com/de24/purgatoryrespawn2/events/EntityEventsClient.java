package com.de24.purgatoryrespawn2.events;

import com.de24.purgatoryrespawn2.PurgatoryRespawn2;
import com.de24.purgatoryrespawn2.entities.DeathSkullEntity;
import com.de24.purgatoryrespawn2.entities.Entities;
import com.de24.purgatoryrespawn2.entities.client.DeathSkullEntityModel;
import com.de24.purgatoryrespawn2.entities.client.DeathSkullRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PurgatoryRespawn2.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityEventsClient {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DeathSkullEntityModel.LAYER_LOCATION, DeathSkullEntityModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Entities.DEATH_SKULL.get(), DeathSkullRenderer::new);
    }

}
