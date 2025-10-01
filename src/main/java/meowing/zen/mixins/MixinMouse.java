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

//#if MC >= 1.21.9
//$$ import net.minecraft.client.input.MouseInput;
//#endif

@Mixin(Mouse.class)
public class MixinMouse {
    @Unique MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    //#if MC >= 1.21.9
    //$$ private void zen$onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
    //#else
    private void zen$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
    //#endif
        if (window == mc.getWindow().getHandle()) {
            boolean pressed = action == 1;
            if (pressed) {
                //#if MC >= 1.21.9
                //$$ if (EventBus.INSTANCE.post(new MouseEvent.Click(input.button()))) ci.cancel();
                //#else
                if (EventBus.INSTANCE.post(new MouseEvent.Click(button))) ci.cancel();
                //#endif
            } else if (action == 0) {
                //#if MC >= 1.21.9
                //$$ if (EventBus.INSTANCE.post(new MouseEvent.Release(input.button()))) ci.cancel();
                //#else
                if (EventBus.INSTANCE.post(new MouseEvent.Release(button))) ci.cancel();
                //#endif
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
