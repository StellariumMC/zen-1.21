package xyz.meowing.zen.mixins;

import xyz.meowing.knit.api.render.KnitResolution;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.MouseEvent;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.input.MouseButtonInfo;
//#endif

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @Inject(
            //#if MC >= 1.21.9
            //$$ method = "onButton",
            //#else
            method = "onPress",
            //#endif
            at = @At("HEAD"),
            cancellable = true
    )
    //#if MC >= 1.21.9
    //$$ private void zen$onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
    //#else
    private void zen$onMouseButton(
            long window,
            int button,
            int action,
            int mods,
            CallbackInfo ci
    ) {
    //#endif
        if (window == KnitResolution.getWindowHandle()) {
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

    @Inject(
            method = "onMove",
            at = @At("HEAD")
    )
    private void zen$onMouseMove(
            long l,
            double d,
            double e,
            CallbackInfo ci
    ) {
        EventBus.INSTANCE.post(new MouseEvent.Move());
    }

    @Inject(
            method = "onScroll",
            at = @At("HEAD")
    )
    private void zen$onMouseScroll(
            long l,
            double d,
            double e,
            CallbackInfo ci
    ) {
        EventBus.INSTANCE.post(new MouseEvent.Scroll(d, e));
    }
}
