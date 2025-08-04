package meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meowing.zen.features.noclutter.HideStatusEffects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    /*
     * Modified from Devonian code
     * Under GPL 3.0 License
     */
    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void zen$renderStatusOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HideStatusEffects.INSTANCE.isEnabled()) ci.cancel();
    }

    @WrapOperation(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I"))
    private int zen$correctXValue(TextRenderer textRenderer, StringVisitable text, Operation<Integer> operation) {
        return textRenderer.getWidth(replaceText((MutableText) text));
    }
}