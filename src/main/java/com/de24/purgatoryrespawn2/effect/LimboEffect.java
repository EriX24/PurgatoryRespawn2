package com.de24.purgatoryrespawn2.effect;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LimboEffect extends MobEffect {
    public LimboEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        // The effect is used to detect players who need reviving
        super.applyEffectTick(livingEntity, amplifier);

        if (livingEntity instanceof Player player) {
            int effectDuration = Objects.requireNonNull(player.getEffect(this)).getDuration();

            // Revive the player
            if (effectDuration <= 1 && !player.level().isClientSide()) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                serverPlayer.setGameMode(GameType.DEFAULT_MODE);
            }

            // Display revive warning
            if (effectDuration <= 1200) {
                player.displayClientMessage(
                        Component.translatable("effect.purgatoryrespawn2.limbo.warning")
                                .withStyle(ChatFormatting.RED),
                        true);
            }

            // Keep the player alive
            if (player.getHealth() < player.getMaxHealth() && !player.level().isClientSide) {
                // Teleport the player
                ServerPlayer serverPlayer = (ServerPlayer) player;
                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                MinecraftServer server = serverPlayer.getServer();

                if (respawnPos != null && server != null) {
                    serverPlayer.teleportTo(Objects.requireNonNull(server.getLevel(serverPlayer.getRespawnDimension())),
                            respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(), 0, 0);
                } else if (server != null) {
                    serverPlayer.teleportTo(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)),
                            0, 64, 0, 0, 0);
                }

                // Broadcast a message
                player.displayClientMessage(
                        Component.translatable("effect.purgatoryrespawn2.limbo.out_of_bounds")
                                .withStyle(ChatFormatting.RED),
                        true);

                // Always be at max health
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
