package meowing.zen.feats.general;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.utils.chatutils;
import meowing.zen.featManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cleanjoin {
    private static final Pattern guild = Pattern.compile("^Guild > (.+) (joined|left).");
    private static final Pattern friend = Pattern.compile("^Friend > (.+) (joined|left).");
    
    public static void initialize() {
        featManager.register(new cleanjoin(), () -> {
            EventBus.register(EventTypes.GameMessageEvent.class, cleanjoin.class, cleanjoin::handleGameMessage);
        });
    }
    
    private static void handleGameMessage(EventTypes.GameMessageEvent event) {
        String text = chatutils.removeFormatting(event.message.getString());
        Matcher m = guild.matcher(text);
        if (m.matches()) {
            chatutils.clientmsg(String.format("§8G %s §b%s", "joined".equals(m.group(2)) ? "§2>>" : "§4<<", m.group(1)));
            event.hide = true;
        }
        m = friend.matcher(text);
        if (m.matches()) {
            chatutils.clientmsg(String.format("§8F %s §b%s", "joined".equals(m.group(2)) ? "§2>>" : "§4<<", m.group(1)));
            event.hide = true;
        }
    }
}