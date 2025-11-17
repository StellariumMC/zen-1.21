package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import xyz.meowing.zen.features.hud.HealthManaPercentage;
import xyz.meowing.zen.features.qol.HideStatusEffects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static xyz.meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(
            method = "renderEffects",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$renderStatusOverlay(
            GuiGraphics context,
            DeltaTracker tickCounter,
            CallbackInfo ci
    ) {
        if (HideStatusEffects.INSTANCE.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void zen$onRenderHealthBar(GuiGraphics context, Player player, int x, int y, int heartRows, int regeneratingHeartIndex, float maxHealth, int health, int displayHealth, int absorption, boolean blinking, CallbackInfo ci) {
        if (HealthManaPercentage.shouldHideVanillaHearts()) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void zen$onRenderFood(GuiGraphics context, Player player, int y, int x, CallbackInfo ci) {
        if (HealthManaPercentage.shouldHideVanillaHearts()) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void zen$onRenderArmor(GuiGraphics context, Player player, int y, int x, int z, int width, CallbackInfo ci) {
        if (HealthManaPercentage.shouldHideVanillaArmor()) ci.cancel();
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"), cancellable = true)
    private void zen$onRenderExperienceBarBackground(CallbackInfo ci) {
        if (HealthManaPercentage.shouldHideExpBar()) ci.cancel();
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"), cancellable = true)
    private void zen$onRenderExperienceLevel(CallbackInfo ci) {
        if (HealthManaPercentage.shouldHideExpBar()) ci.cancel();
    }

    @WrapOperation(
            method = "renderSelectedItemName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"
            )
    )
    private int zen$correctXValue(
            Font textRenderer,
            FormattedText text,
            Operation<Integer> operation
    ) {
        return textRenderer.width(replaceText((MutableComponent) text));
    }
}