package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import xyz.meowing.zen.features.hud.StatsDisplay;
import xyz.meowing.zen.features.qol.HideStatusEffects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
//import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static xyz.meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(Gui.class)
public class MixinGui {
    /*
     * Modified from Devonian code
     * Under GPL 3.0 License
     */
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

//    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
//    private void zen$onRenderHealthBar(GuiGraphics context, Player player, int x, int y, int heartRows, int regeneratingHeartIndex, float maxHealth, int health, int displayHealth, int absorption, boolean blinking, CallbackInfo ci) {
//        if (StatsDisplay.shouldHideVanillaHealth()) ci.cancel();
//    }
//
//    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
//    private void zen$onRenderFood(GuiGraphics context, Player player, int y, int x, CallbackInfo ci) {
//        if (StatsDisplay.shouldHideVanillaHealth()) ci.cancel();
//    }
//
//    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
//    private static void zen$onRenderArmor(GuiGraphics context, Player player, int y, int x, int z, int width, CallbackInfo ci) {
//        if (StatsDisplay.shouldHideVanillaArmor()) ci.cancel();
//    }

    //#if MC >= 1.21.7
    //$$ // explode
    //#else
//    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
//    private void zen$onRenderExperienceBar(GuiGraphics context, int x, CallbackInfo ci) {
//        if (StatsDisplay.shouldHideExpBar()) ci.cancel();
//    }
//
//    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
//    private void zen$onRenderExperienceLevel(GuiGraphics drawContext, DeltaTracker renderTickCounter, CallbackInfo ci) {
//        if (StatsDisplay.shouldHideExpBar()) ci.cancel();
//    }
    //#endif

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