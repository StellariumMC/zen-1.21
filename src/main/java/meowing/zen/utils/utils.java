package meowing.zen.utils;

import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public class utils {
    public static void playSound(SoundEvent Sound, float volume, float pitch) {
        Objects.requireNonNull(MinecraftClient.getInstance().player).playSound(Sound, volume, pitch);
    }
    
    public static void spawnParticle(ParticleEffect particle, Vec3d position, Vec3d velocity) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.particleManager.addParticle(particle, position.x, position.y + 1.0, position.z, velocity.x, velocity.y, velocity.z);
    }

    public static void spawnParticleAtPlayer(ParticleEffect particle, Vec3d velocity) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d playerPos = client.player.getPos();
        client.particleManager.addParticle(particle, playerPos.x, playerPos.y + 1.0, playerPos.z, velocity.x, velocity.y, velocity.z);
    }
    
    public static void spawnParticle(ParticleEffect particle, Vec3d position) {
        spawnParticle(particle, position, Vec3d.ZERO);
    }
}