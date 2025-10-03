package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import xyz.meowing.zen.features.qol.RemoveChatLimit;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(value = ChatHud.class)
public class MixinChatHud {
    @WrapOperation(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int zen$removeMessageLimit(List<?> instance, Operation<Integer> original) {
        if (RemoveChatLimit.INSTANCE.isEnabled()) return original.call(instance) < 100 ? original.call(instance) : 99;
        return original.call(instance);
    }

    @WrapOperation(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int zen$removeVisibleMessageLimit(List<?> instance, Operation<Integer> original) {
        if (RemoveChatLimit.INSTANCE.isEnabled()) return original.call(instance) < 100 ? original.call(instance) : 99;
        return original.call(instance);
    }
}