package meowing.zen.feats.meowing;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import java.util.Objects;
import meowing.zen.Zen;

public class meowsounds {
    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!Zen.getConfig().meowchat) return;
            String content = message.getString().toLowerCase();
            if (content.contains("meow")) playMeowSound();
        });
    }
    private static void playMeowSound() {
        Objects.requireNonNull(MinecraftClient.getInstance().player).playSound(
                SoundEvents.ENTITY_CAT_AMBIENT,
                0.8f, // volume
                1.0f  // pitch
        );
    }
}
