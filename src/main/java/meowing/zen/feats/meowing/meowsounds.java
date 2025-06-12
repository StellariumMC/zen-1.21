package meowing.zen.feats.meowing;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.featManager;
import meowing.zen.utils.utils;
import net.minecraft.sound.SoundEvents;

public class meowsounds {
    private static final meowsounds instance = new meowsounds();
    private meowsounds() {}
    public static void initialize() {
        featManager.register(instance, () -> {
            EventBus.register(EventTypes.GameMessageEvent.class, instance, instance::onGameMessage);
        });
    }

    private void onGameMessage(EventTypes.GameMessageEvent event) {
        String content = event.getPlainText().toLowerCase();
        if (content.contains("meow")) utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f);
    }
}