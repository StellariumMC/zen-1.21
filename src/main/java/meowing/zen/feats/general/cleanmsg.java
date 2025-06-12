package meowing.zen.feats.general;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.utils.chatutils;
import meowing.zen.Zen;
import meowing.zen.featManager;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cleanmsg {
    private static final cleanmsg instance = new cleanmsg();
    private cleanmsg() {}
    private static final Pattern guild = Pattern.compile("^Guild > ?(\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?: (.+)");
    private static final Pattern party = Pattern.compile("^Party > (\\[.+?])? ?(.+?): (.+)");
    private static final Map<String, String> RANK_COLORS = Map.of(
            "Admin", "§c", "Mod", "§2", "Helper", "§9", "GM", "§2",
            "MVP++", "§" + Objects.requireNonNullElse(Zen.getConfig().mvppluspluscolor, "6"),
            "MVP+", "§" + Objects.requireNonNullElse(Zen.getConfig().mvppluscolor, "b"),
            "MVP", "§" + Objects.requireNonNullElse(Zen.getConfig().mvpcolor, "b"),
            "VIP+", "§" + Objects.requireNonNullElse(Zen.getConfig().vippluscolor, "a"),
            "VIP", "§" + Objects.requireNonNullElse(Zen.getConfig().vipcolor, "a")
    );

    public static void initialize() {
        featManager.register(instance, () -> 
            EventBus.register(EventTypes.GameMessageEvent.class, instance, instance::onGameMessage));
    }

    private void onGameMessage(EventTypes.GameMessageEvent event) {     
        String text = chatutils.removeFormatting(event.getPlainText());
        String processed = processGuild(text);
        if (processed == null) processed = processParty(text);
        
        if (processed != null) {
            chatutils.clientmsg(processed, true);
            event.hide();
        }
    }

    private static String processGuild(String text) {
        Matcher m = guild.matcher(text);
        if (!m.matches()) return null;
        String grank = m.group(3) != null ? "§8" + m.group(3) + " " : "";
        return String.format("§2G §8> %s%s%s§f: %s", grank, getRankColor(m.group(1)), m.group(2), m.group(4));
    }

    private static String processParty(String text) {
        Matcher m = party.matcher(text);
        return m.matches() ? String.format("§9P §8> %s%s§f: %s", getRankColor(m.group(1)), m.group(2), m.group(3)) : null;
    }

    private static String getRankColor(String rank) {
        if (rank == null) return "§7";
        return RANK_COLORS.getOrDefault(rank.replaceAll("[\\[\\]]", ""), "§7");
    }
}