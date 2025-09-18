package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.MouseEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    @Unique MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void zen$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            boolean pressed = action == 1;
            if (pressed) {
                if (EventBus.INSTANCE.post(new MouseEvent.Click(button))) ci.cancel();
            } else if (action == 0) {
                if (EventBus.INSTANCE.post(new MouseEvent.Release(button))) ci.cancel();
            }
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void zen$onMouseMove(long l, double d, double e, CallbackInfo ci) {
        EventBus.INSTANCE.post(new MouseEvent.Move());
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void zen$onMouseScroll(long l, double d, double e, CallbackInfo ci) {
        EventBus.INSTANCE.post(new MouseEvent.Scroll(d, e));
    }
}
