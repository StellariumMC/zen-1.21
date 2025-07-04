package meowing.zen.mixins;

import meowing.zen.feats.noclutter.nothunder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.render.entity.state.LightningEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntityRenderer.class)
public class MixinRenderLightning {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LightningEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void onRenderLightning(LightningEntityRenderState lightningEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int meow, CallbackInfo callbackInfo) {
        if (nothunder.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
}
