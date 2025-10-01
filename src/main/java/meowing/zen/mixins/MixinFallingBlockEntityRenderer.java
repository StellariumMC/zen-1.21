package meowing.zen.mixins;

import meowing.zen.features.qol.HideFallingBlocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.minecraft.client.render.entity.state.FallingBlockEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//#endif

@Mixin(FallingBlockEntityRenderer.class)
public class MixinFallingBlockEntityRenderer {
    //#if MC >= 1.21.9
    //$$ @Inject(
    //$$        method = "render(Lnet/minecraft/client/render/entity/state/FallingBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
    //$$        at = @At("HEAD"),
    //$$        cancellable = true
    //$$ )
    //$$ private void zen$onRenderFallingBlocks(FallingBlockEntityRenderState fallingBlockEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
    //$$    if (HideFallingBlocks.INSTANCE.isEnabled()) ci.cancel();
    //$$ }
    //#else
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/FallingBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void zen$onRenderFallingBlocks(FallingBlockEntityRenderState fallingBlockEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo callbackInfo) {
        if (HideFallingBlocks.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
    //#endif
}
