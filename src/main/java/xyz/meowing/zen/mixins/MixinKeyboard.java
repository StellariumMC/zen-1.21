package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.KeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.input.KeyInput;
//#endif

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Unique MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    //#if MC >= 1.21.9
    //$$ private void zen$onKey(long window, int action, KeyInput input, CallbackInfo ci) {
    //#else
    private void zen$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
    //#endif
        if (window == mc.getWindow().getHandle()) {
            if (action == 1) {
                //#if MC >= 1.21.9
                //$$ if (EventBus.INSTANCE.post(new KeyEvent.Press(input.key(), input.scancode(), input.modifiers()))) ci.cancel();
                //#else
                if (EventBus.INSTANCE.post(new KeyEvent.Press(key, scancode, modifiers))) ci.cancel();
                //#endif
            } else if (action == 0) {
                //#if MC >= 1.21.9
                //$$ if (EventBus.INSTANCE.post(new KeyEvent.Release(input.key(), input.scancode(), input.modifiers()))) ci.cancel();
                //#else
                if (EventBus.INSTANCE.post(new KeyEvent.Release(key, scancode, modifiers))) ci.cancel();
                //#endif
            }
        }
    }
}