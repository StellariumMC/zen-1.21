package meowing.zen.feats.meowing;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.featManager;
import meowing.zen.utils.utils;
import net.minecraft.sound.SoundEvents;

public class meowsounds {
    public static void initialize() {
        featManager.register(new meowsounds(), () -> {
            EventBus.register(EventTypes.GameMessageEvent.class, meowsounds.class, meowsounds::handleGameMessage);
        });
    }

    private static void handleGameMessage(EventTypes.GameMessageEvent event) {
        String content = event.message.getString().toLowerCase();
        if (content.contains("meow")) utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f);
    }
}