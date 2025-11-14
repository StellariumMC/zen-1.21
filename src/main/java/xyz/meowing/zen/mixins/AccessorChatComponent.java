package xyz.meowing.zen.mixins;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(ChatComponent.class)
public interface AccessorChatComponent {
    @Accessor("allMessages")
    List<GuiMessage> getMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getVisibleMessages();

    @Invoker("screenToChatX")
    double toChatLineMX(double x);

    @Invoker("screenToChatY")
    double toChatLineMY(double y);

    @Invoker("getMessageLineIndexAt")
    int getMessageLineIdx(double chatLineX, double chatLineY);
}
