package meowing.zen.mixins;

import meowing.zen.feats.noclutter.hidestatuseffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectsDisplay.class)
public class MixinStatusEffectsDisplay {
    @Inject(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"), cancellable = true)
    private void onDrawEffect(DrawContext context, int mx, int my, CallbackInfo callbackInfo) {
        if (hidestatuseffects.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
}
