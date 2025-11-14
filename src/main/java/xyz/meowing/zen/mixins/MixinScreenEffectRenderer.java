package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.qol.HideFireOverlay;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//#endif

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {
    @Inject(
            method = "renderFire",
            at = @At("HEAD"),
            cancellable = true
    )
    //#if MC >= 1.21.9
    //$$ private static void zen$renderFireOverlay(PoseStack poseStack, MultiBufferSource multiBufferSource, TextureAtlasSprite textureAtlasSprite, CallbackInfo ci) {
    //#else
    private static void zen$renderFireOverlay(
            PoseStack matrixStack,
            MultiBufferSource vertexConsumerProvider,
            CallbackInfo ci
    ) {
    //#endif
        if (HideFireOverlay.INSTANCE.isEnabled()) ci.cancel();
    }
}