package meowing.zen.mixins;

import meowing.zen.events.EventBus;
import meowing.zen.events.KeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Unique MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void zen$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            if (action == 1) {
                if (EventBus.INSTANCE.post(new KeyEvent.Press(key, scancode, modifiers))) ci.cancel();
            } else if (action == 0) {
                if (EventBus.INSTANCE.post(new KeyEvent.Release(key, scancode, modifiers))) ci.cancel();
            }
        }
    }
}