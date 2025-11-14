package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.qol.HideThunder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.renderer.state.CameraRenderState;
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//#endif

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(LightningBoltRenderer.class)
public class MixinLightningBoltRenderer {
    //#if MC >= 1.21.9
    //$$ @Inject(
    //$$         method = "submit(Lnet/minecraft/client/renderer/entity/state/LightningBoltRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
    //$$         at = @At("HEAD"),
    //$$         cancellable = true
    //$$ )
    //$$ private void zen$onRenderLightning(LightningBoltRenderState lightningBoltRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
    //#else
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LightningBoltRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$onRenderLightning(
            LightningBoltRenderState lightningEntityRenderState,
            PoseStack matrixStack,
            MultiBufferSource vertexConsumerProvider,
            int meow,
            CallbackInfo ci
    ) {
        //#endif
        if (HideThunder.INSTANCE.isEnabled()) ci.cancel();
    }
}