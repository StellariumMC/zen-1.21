package meowing.zen.mixins;

import meowing.zen.feats.general.removechatlimit;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Mixin(ChatHud.class)
public class MixinChatHistory {
    @Redirect(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int removeMessageLimit(List<?> instance) {
        if (removechatlimit.INSTANCE.isEnabled()) return instance.size() < 100 ? instance.size() : 99;
        return instance.size();
    }

    @Redirect(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int removeVisibleMessageLimit(List<?> instance) {
        if (removechatlimit.INSTANCE.isEnabled()) return instance.size() < 100 ? instance.size() : 99;
        return instance.size();
    }
}