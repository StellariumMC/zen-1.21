package meowing.zen.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import meowing.zen.events.EventBus;
import meowing.zen.events.HudRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class HudRenderMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterHudRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, @Local DrawContext context) {
        EventBus.INSTANCE.post(new HudRenderEvent(context));
    }
}