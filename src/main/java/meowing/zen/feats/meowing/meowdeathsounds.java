package meowing.zen.feats.meowing;

import meowing.zen.Zen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import java.util.Objects;

public class meowdeathsounds {
    public static void initialize() {
        ClientEntityEvents.ENTITY_UNLOAD.register((ent, world) -> {
            if (!Zen.getConfig().meowdeathsounds || ent.isAlive() || ent instanceof ArmorStandEntity || ent.isInvisible() || !(ent instanceof LivingEntity livingEntity && livingEntity.isDead())) return;
            playMeowSound();
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
