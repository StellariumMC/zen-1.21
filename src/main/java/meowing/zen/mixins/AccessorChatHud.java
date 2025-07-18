package meowing.zen.mixins;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(ChatHud.class)
public interface AccessorChatHud {
    @Accessor
    List<ChatHudLine> getMessages();

    @Accessor
    List<ChatHudLine.Visible> getVisibleMessages();

    @Invoker("toChatLineX")
    double toChatLineMX(double x);

    @Invoker("toChatLineY")
    double toChatLineMY(double y);

    @Invoker("getMessageLineIndex")
    int getMessageLineIdx(double chatLineX, double chatLineY);
}
