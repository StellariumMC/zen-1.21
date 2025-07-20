package meowing.zen.mixins;

import meowing.zen.feats.noclutter.HideFog;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void zen$disableFog(Camera camera, FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickProgress, CallbackInfoReturnable<Fog> cir) {
        if (HideFog.INSTANCE.isEnabled()) cir.setReturnValue(new Fog(Float.MAX_VALUE, Float.MAX_VALUE, FogShape.SPHERE, 0.0f, 0.0f, 0.0f, 0.0f));
    }
}
