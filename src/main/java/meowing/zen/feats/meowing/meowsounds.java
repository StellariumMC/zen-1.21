package meowing.zen.feats.meowing;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.featManager;
import meowing.zen.utils.meowutils;

public class meowsounds {
    public static void initialize() {
        featManager.register(new meowsounds(), () -> {
            EventBus.register(EventTypes.GameMessageEvent.class, meowsounds.class, meowsounds::handleGameMessage);
        });
    }

    private static void handleGameMessage(EventTypes.GameMessageEvent event) {
        String content = event.message.getString().toLowerCase();
        if (content.contains("meow")) meowutils.playMeow();
    }
}