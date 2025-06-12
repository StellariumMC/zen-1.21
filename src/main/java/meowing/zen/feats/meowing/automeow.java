package meowing.zen.feats.meowing;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import net.minecraft.client.MinecraftClient;
import meowing.zen.featManager;
import meowing.zen.utils.TickScheduler;
import meowing.zen.utils.chatutils;
import java.util.*;
import java.util.regex.Pattern;

public class automeow {
    private static final automeow instance = new automeow();
    private automeow() {}
    private static final Pattern regex = Pattern.compile("^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$", Pattern.CASE_INSENSITIVE);
    private static final String[] MEOWS = {"mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3"};
    private static final Map<String, String> CHANNELS = Map.of("Party >", "pc", "Guild >", "gc", "Officer >", "oc", "Co-op >", "cc");
    private static final Random random = new Random();

    public static void initialize() {
        TickScheduler.register();
        featManager.register(instance, () -> {
            EventBus.register(EventTypes.GameMessageEvent.class, instance, instance::onGameMessage);
        });
    }

    private void onGameMessage(EventTypes.GameMessageEvent event) {
        if (event.overlay || !regex.matcher(chatutils.removeFormatting(event.getPlainText())).matches()) return;
        
        String content = chatutils.removeFormatting(event.getPlainText());
        String playerName = Objects.requireNonNull(MinecraftClient.getInstance().player).getName().getString();
        if (content.contains("To ") || content.contains(playerName)) return;

        TickScheduler.schedule(random.nextLong(10, 50), () -> {
            String cmd = content.startsWith("From ")
                    ? "msg " + regex.matcher(content).replaceFirst("$1")
                    : CHANNELS.entrySet().stream().filter(e -> content.startsWith(e.getKey())).findFirst().map(Map.Entry::getValue).orElse("ac");
            chatutils.sendcmd(cmd + " " + MEOWS[random.nextInt(MEOWS.length)]);
        });
    }
}