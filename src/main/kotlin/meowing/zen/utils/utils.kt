package meowing.zen.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvent
import net.minecraft.particle.ParticleEffect
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object utils {
    fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        MinecraftClient.getInstance().player?.playSound(sound, volume, pitch)
    }

    fun spawnParticle(particle: ParticleEffect, position: Vec3d, velocity: Vec3d) {
        val client = MinecraftClient.getInstance()
        client.particleManager.addParticle(particle, position.x, position.y + 1.0, position.z, velocity.x, velocity.y, velocity.z)
    }

    fun spawnParticleAtPlayer(particle: ParticleEffect, velocity: Vec3d) {
        val client = MinecraftClient.getInstance()
        val playerPos = client.player?.pos ?: return
        client.particleManager.addParticle(particle, playerPos.x, playerPos.y + 1.0, playerPos.z, velocity.x, velocity.y, velocity.z)
    }

    fun spawnParticle(particle: ParticleEffect, position: Vec3d) {
        spawnParticle(particle, position, Vec3d.ZERO)
    }

    fun renderEntityBox(matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Float, height: Float, r: Float, g: Float, b: Float, a: Float) {
        val box = Box(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2)
        DebugRenderer.drawBox(matrices, vertexConsumers, box, r, g, b, a)
    }
}