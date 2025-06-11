package meowing.zen.feats.general;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import meowing.zen.utils.chatutils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cleanjoin {
    private static final Pattern guild = Pattern.compile("^Guild > (.+) (joined|left).");
    private static final Pattern friend = Pattern.compile("^Friend > (.+) (joined|left).");

    public static void initialize() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String text = chatutils.removeFormatting(message.getString());
            Matcher m = guild.matcher(text);
            if (m.matches()) {
                chatutils.clientmsg(String.format("§8G %s §b%s", "joined".equals(m.group(2)) ? "§2>>" : "§4<<", m.group(1)));
                return false;
            }
            m = friend.matcher(text);
            if (m.matches()) {
                chatutils.clientmsg(String.format("§8F %s §b%s", "joined".equals(m.group(2)) ? "§2>>" : "§4<<", m.group(1)));
                return false;
            }
            return true;
        });
    }
}