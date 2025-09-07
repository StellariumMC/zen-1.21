package meowing.zen.mixins;

import meowing.zen.features.qol.HideStatusEffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(StatusEffectsDisplay.class)
public class MixinStatusEffectsDisplay {
    @Inject(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"), cancellable = true)
    private void zen$onDrawEffect(DrawContext context, int mx, int my, CallbackInfo callbackInfo) {
        if (HideStatusEffects.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
}
