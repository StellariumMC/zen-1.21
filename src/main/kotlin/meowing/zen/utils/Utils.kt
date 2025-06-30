package meowing.zen.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.particle.ParticleTypes
import net.minecraft.particle.SimpleParticleType
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import java.awt.Color

object Utils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()

    fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        MinecraftClient.getInstance().player?.playSound(sound, volume, pitch)
    }

    fun spawnParticle(particle: SimpleParticleType?, x: Double, y: Double, z: Double) {
        spawnParticle(particle, x, y, z, 0.0, 0.0, 0.0)
    }

    fun spawnParticle(particle: SimpleParticleType?, x: Double, y: Double, z: Double, velocityX: Double, velocityY: Double, velocityZ: Double) {
        val mc = MinecraftClient.getInstance()
        mc.world?.addParticleClient(ParticleTypes.FLAME, x, y, z, velocityX, velocityY, velocityZ)
    }

    fun spawnParticleAtPlayer(particle: SimpleParticleType?, velocityX: Double, velocityY: Double, velocityZ: Double) {
        val mc = MinecraftClient.getInstance()
        mc.player?.let { player ->
            spawnParticle(
                particle,
                player.x,
                player.y + 1.0,
                player.z,
                velocityX, velocityY, velocityZ
            )
        }
    }

    fun showTitle(title: String?, subtitle: String?, duration: Int) {
        val mc = MinecraftClient.getInstance()
        mc.inGameHud.setTitle(title?.let { Text.literal(it) })
        mc.inGameHud.setSubtitle(subtitle?.let { Text.literal(it) })
    }

    fun showTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val mc = MinecraftClient.getInstance()
        mc.inGameHud.setTitleTicks(fadeIn, stay, fadeOut)
        mc.inGameHud.setTitle(Text.literal(title))
        mc.inGameHud.setSubtitle(Text.literal(subtitle))
    }

    fun String.removeFormatting(): String {
        return this.replace(Regex("[§&][0-9a-fk-or]", RegexOption.IGNORE_CASE), "")
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun getPartialTicks(): Float = MinecraftClient.getInstance().renderTickCounter.getTickProgress(true)

    fun Color.toColorInt(): Int {
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    fun Int.toColorFloat(): Float {
        return this / 255f
    }

    inline fun <reified R> Any.getField(name: String): R = javaClass.getDeclaredField(name).apply { isAccessible = true }[this] as R
}