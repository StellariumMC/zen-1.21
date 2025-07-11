package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors

object Render2D {
    fun renderString(context: DrawContext, text: String, x: Float, y: Float, scale: Float) {
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1.0f)
        context.drawText(mc.textRenderer, text, 0, 0, Colors.WHITE, false)
        context.matrices.pop()
    }

    fun renderStringWithShadow(context: DrawContext, text: String, x: Float, y: Float, scale: Float) {
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1.0f)
        context.drawText(mc.textRenderer, text, 0, 0, Colors.WHITE, true)
        context.matrices.pop()
    }
}