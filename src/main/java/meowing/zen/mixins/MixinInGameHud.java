package meowing.zen.mixins;

import meowing.zen.feats.noclutter.HideStatusEffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void zen$renderStatusOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HideStatusEffects.INSTANCE.isEnabled()) ci.cancel();
    }
}
