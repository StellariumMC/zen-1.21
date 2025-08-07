package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Colors
import kotlin.math.max

object Render2D {

    fun renderString(context: DrawContext, text: String, x: Float, y: Float, scale: Float, colors: Int = Colors.WHITE, shadow: Boolean = false) {
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1.0f)
        context.drawText(mc.textRenderer, text, 0, 0, colors, shadow)
        context.matrices.pop()
    }

    fun renderStringWithShadow(context: DrawContext, text: String, x: Float, y: Float, scale: Float) {
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1.0f)
        context.drawText(mc.textRenderer, text, 0, 0, Colors.WHITE, true)
        context.matrices.pop()
    }

    fun renderItem(context: DrawContext, item: ItemStack, x: Float, y: Float, scale: Float) {
        val matrixStack = context.matrices
        matrixStack.push()
        matrixStack.translate(x, y, 0.0f)
        matrixStack.scale(scale, scale, 1f)
        context.drawItem(item, 0, 0)
        matrixStack.pop()
    }

    fun String.width(): Int {
        val newlines = this.split("\n")
        if (newlines.size <= 1) return mc.textRenderer.getWidth(this.removeFormatting())

        var maxWidth = 0

        for (line in newlines)
            maxWidth = max(maxWidth, mc.textRenderer.getWidth(line.removeFormatting()))

        return maxWidth
    }

    fun String.height(): Int {
        val newlines = this.split("\n")
        if (newlines.size <= 1) return mc.textRenderer.fontHeight

        return mc.textRenderer.fontHeight * (newlines.size + 1)
    }
}