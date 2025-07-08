package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
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
        mc.world?.addParticleClient(ParticleTypes.FLAME, x, y, z, velocityX, velocityY, velocityZ)
    }

    fun spawnParticleAtPlayer(particle: SimpleParticleType?, velocityX: Double, velocityY: Double, velocityZ: Double) {
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
        mc.inGameHud.setTitle(title?.let { Text.literal(it) })
        mc.inGameHud.setSubtitle(subtitle?.let { Text.literal(it) })
    }

    fun showTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        mc.inGameHud.setTitleTicks(fadeIn, stay, fadeOut)
        mc.inGameHud.setTitle(Text.literal(title))
        mc.inGameHud.setSubtitle(Text.literal(subtitle))
    }

    fun String?.removeFormatting(): String {
        if (this == null) return ""
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

    fun Map<*, *>.toColorFromMap(): Color? {
        return try {
            val rgbValue = (this["value"] as? Number)?.toInt() ?: return null
            val alpha = ((this["falpha"] as? Number)?.toDouble() ?: 1.0).coerceIn(0.0, 1.0)

            val r = (rgbValue shr 16) and 0xFF
            val g = (rgbValue shr 8) and 0xFF
            val b = rgbValue and 0xFF
            val a = (alpha * 255).toInt()

            Color(r, g, b, a)
        } catch (e: Exception) {
            null
        }
    }

    fun List<*>.toColorFromList(): Color? {
        return try {
            if (size < 4) return null
            Color(
                (this[0] as? Number)?.toInt() ?: return null,
                (this[1] as? Number)?.toInt() ?: return null,
                (this[2] as? Number)?.toInt() ?: return null,
                (this[3] as? Number)?.toInt() ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified R> Any.getField(name: String): R = javaClass.getDeclaredField(name).apply { isAccessible = true }[this] as R
}