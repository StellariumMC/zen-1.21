package meowing.zen.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Objects;

public class chatutils {
    public static void clientmsg(String message) {
        Objects.requireNonNull(MinecraftClient.getInstance().player).sendMessage(Text.literal(message), false);
    }
    public static void sendmsg(String message) {
        Objects.requireNonNull(MinecraftClient.getInstance().player).networkHandler.sendChatMessage(message);
    }
    public static void sendcmd(String command) {
        Objects.requireNonNull(MinecraftClient.getInstance().player).networkHandler.sendChatCommand(command);
    }
    public static String removeFormatting(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
}
