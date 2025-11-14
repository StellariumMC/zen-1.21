package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.qol.HideStatusEffects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(EffectsInInventory.class)
public class MixinEffectsInInventory {
    @Inject(
            method = "renderEffects(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$onDrawEffect(
            GuiGraphics context,
            int mx,
            int my,
            CallbackInfo callbackInfo
    ) {
        if (HideStatusEffects.INSTANCE.isEnabled()) callbackInfo.cancel();
    }
}
