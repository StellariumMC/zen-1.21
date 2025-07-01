package meowing.zen.mixins;

import kotlin.Unit;
import meowing.zen.Zen;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinRenderPlayerEntity {
    @Unique private static boolean enabled = false;
    @Unique private static float x = 1.0f;
    @Unique private static float y = 1.0f;
    @Unique private static float z = 1.0f;
    static {
        Zen.Companion.registerCallback("customsize", value -> {
            enabled = (Boolean) value;
            return Unit.INSTANCE;
        });
        Zen.Companion.registerCallback("customX", value -> {
            x = ((Double) value).floatValue();
            return Unit.INSTANCE;
        });
        Zen.Companion.registerCallback("customY", value -> {
            y = ((Double) value).floatValue();
            return Unit.INSTANCE;
        });
        Zen.Companion.registerCallback("customZ", value -> {
            z = ((Double) value).floatValue();
            return Unit.INSTANCE;
        });
    }
    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"))
    private void scalePlayerModel(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, CallbackInfo ci) {
        if (enabled) matrixStack.scale(x, y, z);
    }
}

