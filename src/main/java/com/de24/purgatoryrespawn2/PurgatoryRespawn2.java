package com.de24.purgatoryrespawn2;

import com.de24.purgatoryrespawn2.effect.LimboEffect;
import com.de24.purgatoryrespawn2.effect.ModEffects;
import com.de24.purgatoryrespawn2.entities.DeathSkullEntity;
import com.de24.purgatoryrespawn2.entities.Entities;
import com.de24.purgatoryrespawn2.entities.client.DeathSkullRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.checkerframework.checker.units.qual.A;

import java.util.Objects;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PurgatoryRespawn2.MODID)
public class PurgatoryRespawn2 {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "purgatoryrespawn2";
    // Directly reference a slf4j logger

    public PurgatoryRespawn2(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the entities
        Entities.register(modEventBus);

        // Register the effects
        ModEffects.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the death event
        MinecraftForge.EVENT_BUS.register(MyStaticForgeEventHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        // LOGGER.info("HELLO from server starting");
    }

    // Event handler
    public static class MyStaticForgeEventHandler {
        @SubscribeEvent
        public static void onEntityDeath(LivingDeathEvent event) {
            // Check if a player died
            if (event.getEntity() instanceof Player) {
                // Check if it's on the server side
                if (!event.getEntity().level().isClientSide()) {
                    ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
                    ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                    GameType playerGameMode = serverPlayer.gameMode.getGameModeForPlayer();

                    // Check if the player is in survival or adventure mode
                    if (playerGameMode == GameType.SURVIVAL || playerGameMode == GameType.ADVENTURE) {
                        // Summon a death skull when a player dies
                        DeathSkullEntity deathSkull = new DeathSkullEntity(Entities.DEATH_SKULL.get(), event.getEntity().level());
                        deathSkull.setPos(event.getEntity().position().add(0, 0.75, 0));
                        deathSkull.playerUUID = event.getEntity().getUUID();
                        deathSkull.setCustomName(event.getEntity().getName());

                        event.getEntity().level().addFreshEntity(deathSkull);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                GameType playerGameMode = serverPlayer.gameMode.getGameModeForPlayer();

                if (playerGameMode == GameType.SURVIVAL || playerGameMode == GameType.ADVENTURE) {
                    serverPlayer.setGameMode(GameType.SPECTATOR);

                    // TODO: Decide on a reasonable number for this max health reduction
                    // Reduce health
                    AttributeModifier attributeModifier = new AttributeModifier("generic.max_health", -14, AttributeModifier.Operation.ADDITION);
                    serverPlayer.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(attributeModifier);

                    serverPlayer.addEffect(new MobEffectInstance(ModEffects.LIMBO.get(), 6000));
                }
            }
        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
            // Heal the player every half a day
            Level level = event.player.level();

            if (!level.isClientSide && event.phase == TickEvent.Phase.START && (level.getGameTime() % 12000) == 0) {
                ServerPlayer serverPlayer = (ServerPlayer) event.player;
                GameType playerGameMode = serverPlayer.gameMode.getGameModeForPlayer();

                if (serverPlayer.getAttribute(Attributes.MAX_HEALTH).getValue() < 20 && playerGameMode != GameType.SPECTATOR) {
                    AttributeModifier attributeModifier = new AttributeModifier("generic.max_health", 2, AttributeModifier.Operation.ADDITION);
                    Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(attributeModifier);
                }
            }
        }

        @SubscribeEvent
        public static void entityLeaveEvent(EntityLeaveLevelEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof DeathSkullEntity && !event.getLevel().isClientSide()) {
                ServerLevel serverLevel = (ServerLevel) entity.level();
                ChunkPos chunkPos = entity.chunkPosition();

                // Unload the chunk
                ForgeChunkManager.forceChunk(serverLevel, PurgatoryRespawn2.MODID, entity.getUUID(), chunkPos.x, chunkPos.z, false, true);
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(Entities.DEATH_SKULL.get(), DeathSkullRenderer::new);
            // Some client setup code
            // LOGGER.info("HELLO FROM CLIENT SETUP");
            // LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
