package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.qol.HideThunder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.render.entity.state.LightningEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//#endif

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(LightningEntityRenderer.class)
public class MixinLightningEntityRenderer {
    //#if MC >= 1.21.9
    //$$ @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LightningEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    //$$ private void zen$onRenderLightning(LightningEntityRenderState lightningEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
    //#else
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LightningEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void zen$onRenderLightning(LightningEntityRenderState lightningEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int meow, CallbackInfo ci) {
    //#endif
        if (HideThunder.INSTANCE.isEnabled()) ci.cancel();
    }
}
