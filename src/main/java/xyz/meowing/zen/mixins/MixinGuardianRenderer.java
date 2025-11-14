package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.RenderEvent;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.renderer.state.CameraRenderState;
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//#endif

@Mixin(GuardianRenderer.class)
public class MixinGuardianRenderer {
    @Unique
    private Guardian zen$currentEntity;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/Guardian;Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;F)V",
            at = @At("HEAD")
    )
    private void zen$captureEntity(
            Guardian entity,
            GuardianRenderState renderState,
            float tickDelta,
            CallbackInfo ci
    ) {
        this.zen$currentEntity = entity;
    }

    //#if MC >= 1.21.9
    //$$ @Inject(
    //$$         method = "submit(Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
    //$$                 ordinal = 0
    //$$         ),
    //$$         cancellable = true
    //$$ )
    //$$ private void zen$guardianLaserRender(GuardianRenderState guardianRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
    //#else
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void zen$guardianLaserRender(
            GuardianRenderState renderState,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        //#endif
        if (zen$currentEntity != null && EventBus.INSTANCE.post(new RenderEvent.GuardianLaser(zen$currentEntity, zen$currentEntity.getActiveAttackTarget()))) {
            ci.cancel();
        }
    }
}