package xyz.meowing.zen.utils

import net.minecraft.text.Text
import net.minecraft.util.Formatting

object GradientUtils {
    fun createText(text: String, startColor: Int, endColor: Int, prefix: String?, suffix: String?): Text {
        var result = Text.empty()

        if (!prefix.isNullOrEmpty()) {
            result = result.append(Text.literal(prefix).formatted(Formatting.GRAY))
        }

        for (i in text.indices) {
            val color = interpolateColor(startColor, endColor, i, text.length - 1)
            result = result.append(Text.literal(text[i].toString()).withColor(color))
        }

        if (!suffix.isNullOrEmpty()) {
            result = result.append(Text.literal(suffix).formatted(Formatting.GRAY))
        }

        return result
    }

    private fun interpolateColor(startColor: Int, endColor: Int, currentIndex: Int, totalLength: Int): Int {
        if (totalLength == 0) return startColor

        val ratio = currentIndex.toDouble() / totalLength

        val startR = (startColor shr 16) and 0xFF
        val startG = (startColor shr 8) and 0xFF
        val startB = startColor and 0xFF

        val endR = (endColor shr 16) and 0xFF
        val endG = (endColor shr 8) and 0xFF
        val endB = endColor and 0xFF

        val r = (startR + (endR - startR) * ratio).toInt()
        val g = (startG + (endG - startG) * ratio).toInt()
        val b = (startB + (endB - startB) * ratio).toInt()

        return (r shl 16) or (g shl 8) or b
    }
}