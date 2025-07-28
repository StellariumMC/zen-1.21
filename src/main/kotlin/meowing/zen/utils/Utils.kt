package meowing.zen.utils

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import meowing.zen.Zen.Companion.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.particle.ParticleTypes
import net.minecraft.particle.SimpleParticleType
import net.minecraft.sound.SoundEvent
import org.apache.commons.lang3.SystemUtils
import java.awt.Color

object Utils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()

    inline val partialTicks get() = mc.renderTickCounter.getTickProgress(true)
    inline val window get() = mc.window
    inline val MouseX get() = mc.mouse.x * window.scaledWidth / window.width
    inline val MouseY get() = mc.mouse.y * window.scaledWidth / window.width

    fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        MinecraftClient.getInstance().player?.playSound(sound, volume, pitch)
    }

    fun spawnParticle(particle: SimpleParticleType?, x: Double, y: Double, z: Double) {
        spawnParticle(particle, x, y, z, 0.0, 0.0, 0.0)
    }

    fun spawnParticle(particle: SimpleParticleType?, x: Double, y: Double, z: Double, velocityX: Double, velocityY: Double, velocityZ: Double) {
        mc.world?.addParticleClient(ParticleTypes.FLAME, x, y, z, velocityX, velocityY, velocityZ)
    }

    fun String?.removeFormatting(): String {
        if (this == null) return ""
        return this.replace(Regex("[§&][0-9a-fk-or]", RegexOption.IGNORE_CASE), "")
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun Color.toColorInt(): Int {
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    fun Int.toColorFloat(): Float {
        return this / 255f
    }

    fun Color.toFloatArray(): FloatArray {
        return floatArrayOf(red / 255f, green / 255f, blue / 255f)
    }

    fun Map<*, *>.toColorFromMap(): Color? {
        return try {
            val r = (get("r") as? Number)?.toInt() ?: 255
            val g = (get("g") as? Number)?.toInt() ?: 255
            val b = (get("b") as? Number)?.toInt() ?: 255
            val a = (get("a") as? Number)?.toInt() ?: 255
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

    fun decodeRoman(roman: String): Int {
        val values = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var result = 0
        var prev = 0

        for (char in roman.reversed()) {
            val current = values[char] ?: 0
            if (current < prev) result -= current
            else result += current
            prev = current
        }
        return result
    }

    fun Long.toFormattedDuration(short: Boolean = false): String {
        val seconds = this / 1000
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        if (short) {
            return when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${remainingSeconds}s"
            }
        }

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (remainingSeconds > 0) append("${remainingSeconds}s")
        }.trimEnd()
    }

    fun createBlock(radius: Float = 0f): UIComponent {
        return if (SystemUtils.IS_OS_MAC_OSX) UIBlock() else UIRoundedRectangle(radius)
    }

    inline fun <reified R> Any.getField(name: String): R = javaClass.getDeclaredField(name).apply { isAccessible = true }[this] as R
}