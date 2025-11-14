package xyz.meowing.zen.utils

import net.minecraft.network.chat.Component

object GradientUtils {
    fun createText(text: String, startColor: Int, endColor: Int, prefix: Component?, suffix: Component?): Component {
        var result = Component.empty()

        prefix?.takeIf { it.string.isNotEmpty() }?.let {
            result = result.append(it)
        }

        text.forEachIndexed { i, char ->
            val color = fineShit(startColor, endColor, i, text.lastIndex)
            result = result.append(Component.literal(char.toString()).withColor(color))
        }

        suffix?.takeIf { it.string.isNotEmpty() }?.let {
            result = result.append(it)
        }

        return result
    }

    private fun fineShit(startColor: Int, endColor: Int, currentIndex: Int, totalLength: Int): Int {
        if (totalLength == 0) return startColor

        val ratio = currentIndex.toDouble() / totalLength

        val r = interp(startColor shr 16, endColor shr 16, ratio)
        val g = interp(startColor shr 8, endColor shr 8, ratio)
        val b = interp(startColor, endColor, ratio)

        return (r shl 16) or (g shl 8) or b
    }

    private fun interp(start: Int, end: Int, ratio: Double): Int = ((start and 0xFF) + ((end and 0xFF) - (start and 0xFF)) * ratio).toInt()
}