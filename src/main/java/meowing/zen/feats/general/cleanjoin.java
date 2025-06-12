package meowing.zen.feats.general;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.utils.chatutils;
import meowing.zen.featManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cleanjoin {
    private static final cleanjoin instance = new cleanjoin();
    private cleanjoin() {}
    private static final Pattern guild = Pattern.compile("^Guild > (.+) (joined|left).");
    private static final Pattern friend = Pattern.compile("^Friend > (.+) (joined|left).");
    
    public static void initialize() {
        featManager.register(instance, () -> {
            EventBus.register(EventTypes.GameMessageEvent.class, instance, instance::handleGameMessage);
        });
    }
    
    private void handleGameMessage(EventTypes.GameMessageEvent event) {
        String text = chatutils.removeFormatting(event.getPlainText());
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