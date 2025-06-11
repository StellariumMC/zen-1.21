package meowing.zen.utils;

import java.util.Objects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

public class meowutils {
    public static void playMeow() {
        Objects.requireNonNull(MinecraftClient.getInstance().player).playSound(
            SoundEvents.ENTITY_CAT_AMBIENT,
            0.8f,
            1.0f
        );
    }
}
