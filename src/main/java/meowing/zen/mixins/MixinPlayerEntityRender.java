package meowing.zen.mixins;

import meowing.zen.Zen;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRender {
    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"))
    private void scalePlayerModel(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, CallbackInfo ci) {
        if (Zen.Companion.getConfig().getCustomsize())
            matrixStack.scale(Zen.Companion.getConfig().getCustomX(), Zen.Companion.getConfig().getCustomY(), Zen.Companion.getConfig().getCustomZ());
    }
}

