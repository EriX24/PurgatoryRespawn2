package com.de24.purgatoryrespawn2.entities.client;

import com.de24.purgatoryrespawn2.PurgatoryRespawn2;
import com.de24.purgatoryrespawn2.entities.DeathSkullEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DeathSkullRenderer extends EntityRenderer<DeathSkullEntity> {
    ModelLayerLocation deathSkullLayer;
    EntityModel<DeathSkullEntity> deathSkullModel;
    ResourceLocation deathSkullResource;

    public DeathSkullRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);

        // Get the resource
        deathSkullResource = new ResourceLocation(PurgatoryRespawn2.MODID, "textures/entities/death_skull.png");

        // Get the layer
        deathSkullLayer = DeathSkullEntityModel.LAYER_LOCATION;

        // Bake the layer
        ModelPart modelPart = pContext.bakeLayer(deathSkullLayer);

        // Get the model
        deathSkullModel = new DeathSkullEntityModel<>(modelPart);
    }

    @Override
    public void render(@NotNull DeathSkullEntity pEntity, float pEntityYaw, float pPartialTick, @NotNull PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        // Push pose, no clue what this does
        pPoseStack.pushPose();

        // Make rotations work I guess
        pEntity.setYRot(0.0F);
//        pPoseStack.rotateAround(Axis.YP.rotationDegrees((pEntity.age * 20 + pPartialTick) * 2), 0, 0, 0);
//        System.out.println(150);
        pPoseStack.mulPose(Axis.YP.rotation((pEntity.age * 20 + pPartialTick) / 25));
//        pPoseStack.
//        pEntityYaw = (pEntity.age * 20 + pPartialTick) * 2;

        // Do scaling and position changes to make the model show as intended
        pPoseStack.scale(1.0F, -1.0F, -1.0F);
        pPoseStack.translate(0F, -1.25F, 0F);

        // Get a vertex consumer
        VertexConsumer vertexConsumer = pBuffer.getBuffer(deathSkullModel.renderType(deathSkullResource));

        // Render the model
        deathSkullModel.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // Pop pose, no clue what this does
        pPoseStack.popPose();

        // Call super.render at the end
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DeathSkullEntity deathSkullEntity) {
        return new ResourceLocation(PurgatoryRespawn2.MODID, "textures/entities/death_skull.png");
    }
}
