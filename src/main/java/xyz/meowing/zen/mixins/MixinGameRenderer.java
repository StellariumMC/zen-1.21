package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.GuiEvent;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void zen$afterHudRender(
            DeltaTracker tickCounter,
            boolean tick,
            CallbackInfo ci,
            @Local GuiGraphics context
    ) {
        EventBus.INSTANCE.post(new GuiEvent.Render.HUD(context, GuiEvent.RenderType.Pre));
    }
}