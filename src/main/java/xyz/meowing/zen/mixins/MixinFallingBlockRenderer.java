package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.qol.HideFallingBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
public class MixinFallingBlockRenderer {
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$onRenderFallingBlocks(
            FallingBlockRenderState fallingBlockEntityRenderState,
            PoseStack matrixStack,
            MultiBufferSource vertexConsumerProvider,
            int i,
            CallbackInfo callbackInfo
    ) {
        if (HideFallingBlocks.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
}
