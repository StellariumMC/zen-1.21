package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import xyz.meowing.zen.features.qol.RemoveChatLimit;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(ChatComponent.class)
public class MixinChatComponent {
    @WrapOperation(
            method = "addMessageToQueue(Lnet/minecraft/client/GuiMessage;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I"
            )
    )
    private int zen$removeMessageLimit(
            List<?> instance,
            Operation<Integer> original
    ) {
        if (RemoveChatLimit.INSTANCE.isEnabled()) return original.call(instance) < 100 ? original.call(instance) : 99;
        return original.call(instance);
    }

    @WrapOperation(
            method = "addMessageToDisplayQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I"
            )
    )
    private int zen$removeVisibleMessageLimit(
            List<?> instance,
            Operation<Integer> original
    ) {
        if (RemoveChatLimit.INSTANCE.isEnabled()) return original.call(instance) < 100 ? original.call(instance) : 99;
        return original.call(instance);
    }
}