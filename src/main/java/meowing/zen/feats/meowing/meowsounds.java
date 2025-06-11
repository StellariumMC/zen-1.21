package meowing.zen.feats.meowing;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import meowing.zen.Zen;
import meowing.zen.utils.meowutils;

public class meowsounds {
    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!Zen.getConfig().meowchat) return;
            String content = message.getString().toLowerCase();
            if (content.contains("meow")) meowutils.playMeow();
        });
    }
}
