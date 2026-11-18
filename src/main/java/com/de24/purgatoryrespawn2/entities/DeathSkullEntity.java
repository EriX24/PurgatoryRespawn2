package com.de24.purgatoryrespawn2.entities;

import com.de24.purgatoryrespawn2.PurgatoryRespawn2;
import com.de24.purgatoryrespawn2.effect.LimboEffect;
import com.de24.purgatoryrespawn2.effect.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.level.NoteBlockEvent;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

public class DeathSkullEntity extends Entity {
    public float age;
    public float reviveCountdown = 5;

    // No clue if this works, this is a fallback anyway
    public UUID playerUUID = UUID.randomUUID();
    public boolean forceLoaded = false;

    public DeathSkullEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        // Get NBT data
        this.age = compoundTag.getShort("Age");

        if (compoundTag.hasUUID("PlayerUUID")) {
            this.playerUUID = compoundTag.getUUID("PlayerUUID");
        }

        this.reviveCountdown = compoundTag.getFloat("ReviveCountdown");
        this.forceLoaded = compoundTag.getBoolean("ForceLoaded");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        // Store NBT data
        compoundTag.putShort("Age", (short) this.age);
        compoundTag.putUUID("PlayerUUID", playerUUID);
        compoundTag.putFloat("ReviveCountdown", reviveCountdown);
        compoundTag.putBoolean("ForceLoaded", forceLoaded);
    }

    @Override
    public void tick() {
        // Increase age
        this.age += (float) (1.0 / 20.0);

        // Ensure the level is server side
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            ChunkPos chunkPos = this.chunkPosition();
            ServerPlayer targetPlayer = Objects.requireNonNull(this.getServer()).getPlayerList().getPlayer(this.playerUUID);

            // Load the chunk
            if (!forceLoaded) {
                ForgeChunkManager.forceChunk(serverLevel, PurgatoryRespawn2.MODID, this.uuid, chunkPos.x, chunkPos.z, true, true);

                forceLoaded = true;
            }

            // Check if the player is in the server, or if they exist
            ServerPlayer playerEntity = this.getServer().getPlayerList().getPlayer(playerUUID);

            // Check if the player is on the respawn screen, or if the player is alive
            if (playerEntity != null) {
                if (!(playerEntity.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) &&
                        !(playerEntity.hasEffect(ModEffects.LIMBO.get())) &&
                        playerEntity.getHealth() <= 0) {
                    // The player is on the respawn screen
                    List<Player> nearbyPlayers = this.getNearbyPlayers(this.level());

                    for (Player player : nearbyPlayers) {
                        player.displayClientMessage(Component.translatable("entity.purgatoryrespawn2.death_skull.await_respawn"), true);
                    }

                    return;
                } else if (!(playerEntity.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) &&
                        !(playerEntity.hasEffect(ModEffects.LIMBO.get())) &&
                        playerEntity.getHealth() > 0) {
                    // Show particles
                    serverLevel.sendParticles(ParticleTypes.ASH.getType(),
                            this.getX(), this.getY(), this.getZ(),
                            100,
                            (Math.random() / 2) - 0.25, (Math.random() / 2) - 0.25, (Math.random() / 2) - 0.25,
                            1);

                    // The player is alive and this should self-destruct
                    this.discard();

                    return;
                }
            }

            if (this.reviveCountdown > 0) {
                // Display revive instructions to nearby players
                List<Player> nearbyEntities = this.getNearbyPlayers(this.level());

                boolean playerCrouched = false;

                for (Player indivisualPlayer : nearbyEntities) {
                    // Get the necessary message stuff
                    String reviveMessageOne = Component.translatable("entity.purgatoryrespawn2.death_skull.revive_1").getString();
                    String reviveMessageTwo = Component.translatable("entity.purgatoryrespawn2.death_skull.revive_2").getString();
                    String reviveMessage = reviveMessageOne + Float.toString((float) Math.round(this.reviveCountdown * 10) / 10) + reviveMessageTwo;

                    // Show the text
                    indivisualPlayer.displayClientMessage(Component.literal(reviveMessage), true);

                    // Check if the player is crouched
                    if (indivisualPlayer.isCrouching()) {
                        playerCrouched = true;
                    }

                    // Slowly revive the player
                    if (playerCrouched) {
                        this.reviveCountdown -= 0.05F;
                    } else if (this.reviveCountdown > 0) {
                        this.reviveCountdown = 5.0F;
                    }
                }

                if (nearbyEntities.isEmpty() && this.reviveCountdown > 0) {
                    this.reviveCountdown = 5;
                }
            } else if (this.reviveCountdown <= 0 && targetPlayer != null) {
                // Some variables
                List<Player> nearbyEntities = this.getNearbyPlayers(this.level());
                Vec3 position = this.position();

                // Broadcast the revived message to nearby players
                for (Player player : nearbyEntities) {
                    player.displayClientMessage(Component.translatable("entity.purgatoryrespawn2.death_skull.player_revived"), true);
                }

                // Change the dimension
                ResourceKey<Level> resourceKey = this.level().dimension();
                ServerLevel currentDimension = getServer().getLevel(resourceKey);

                if (currentDimension == null) {
                    // Cancel this function if something is wrong to prevent a crash
                    return;
                }

                // Teleport the player and remove the limbo effect
                targetPlayer.teleportTo(currentDimension, position.x, position.y - 0.75, position.z, 0, 0);
                if (targetPlayer.hasEffect(ModEffects.LIMBO.get())) {
                    targetPlayer.removeEffect(ModEffects.LIMBO.get());
                }

                // Show particles
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                        20,
                        0, 0, 0,
                        1);

                // Change game modes
                targetPlayer.setGameMode(GameType.SURVIVAL);

                // Discard this entity
                this.discard();

            } else if (this.reviveCountdown <= 0) {
                List<Player> nearbyEntities = getNearbyPlayers(this.level());

                // Show the player connecting message
                for (Player player : nearbyEntities) {
                    player.displayClientMessage(Component.translatable("entity.purgatoryrespawn2.death_skull.reconnect"), true);
                }

                // Display some particles if the player is being revived
                if (((float) (Math.round(this.age * 10) / 10) % 1) == 0) {
                    Vec3 position = this.position();
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            position.x, position.y + 0.25, position.z,
                            1,
                            (Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10,
                            0.25);
                }
            }
        }
    }

    protected List<Player> getNearbyPlayers(Level level) {
        // Get all nearby players
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, AABB.ofSize(this.position(),
                5, 5, 5));

        // Remove the dead player so they don't see the waiting for player to click respawn
        for (Player player : nearbyPlayers) {
            if (player.getUUID() == this.playerUUID) {
                nearbyPlayers.remove(player);
                break;
            }
        }

        // Return
        return nearbyPlayers;
    }

    public boolean shouldShowName() {
        return true;
    }

    protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.25F;
    }
}
