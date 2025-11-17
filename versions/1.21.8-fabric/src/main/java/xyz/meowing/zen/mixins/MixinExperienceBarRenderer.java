package xyz.meowing.zen.mixins;

import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.features.hud.HealthManaPercentage;

@Mixin(ExperienceBarRenderer.class)
public class MixinExperienceBarRenderer {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void zen$onRenderExperienceBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (HealthManaPercentage.shouldHideExpBar()) ci.cancel();
    }
}