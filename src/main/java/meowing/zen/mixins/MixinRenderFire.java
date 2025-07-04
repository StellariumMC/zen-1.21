package meowing.zen.mixins;

import meowing.zen.feats.noclutter.hidefireoverlay;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinRenderFire {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderFireOverlay(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, CallbackInfo callbackInfo) {
        if (hidefireoverlay.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
}
