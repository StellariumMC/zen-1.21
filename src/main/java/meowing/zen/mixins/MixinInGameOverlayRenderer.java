package meowing.zen.mixins;

import meowing.zen.features.qol.HideFireOverlay;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.texture.Sprite;
//#endif

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    //#if MC >= 1.21.9
    //$$ private static void zen$renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite, CallbackInfo ci) {
    //#else
    private static void zen$renderFireOverlay(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, CallbackInfo ci) {
    //#endif
        if (HideFireOverlay.INSTANCE.isEnabled()) ci.cancel();
    }
}
