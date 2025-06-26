package meowing.zen.hud

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper

abstract class HudRenderer(val element: HudElement) {
    abstract fun render(context: DrawContext, tickCounter: RenderTickCounter)
    abstract fun getPreviewSize(): Pair<Float, Float>
}

class TextHudRenderer(element: HudElement, private val textProvider: () -> String) : HudRenderer(element) {
    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        val text = textProvider()
        val actualX = element.getActualX(mc.window.scaledWidth)
        val actualY = element.getActualY(mc.window.scaledHeight)

        context.matrices.push()
        context.matrices.translate(actualX.toDouble(), actualY.toDouble(), 0.0)
        context.matrices.scale(element.scale, element.scale, 1.0f)
        context.drawText(mc.textRenderer, text, 0, 0, Colors.WHITE, false)
        context.matrices.pop()
    }

    override fun getPreviewSize(): Pair<Float, Float> {
        val text = textProvider()
        return Pair(
            mc.textRenderer.getWidth(text) * element.scale,
            mc.textRenderer.fontHeight * element.scale
        )
    }
}

class BoxHudRenderer(element: HudElement) : HudRenderer(element) {
    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        val actualX = element.getActualX(mc.window.scaledWidth).toInt()
        val actualY = element.getActualY(mc.window.scaledHeight).toInt()

        context.fill(
            actualX, actualY,
            actualX + (element.width * element.scale).toInt(),
            actualY + (element.height * element.scale).toInt(),
            ColorHelper.getArgb(128, 255, 255, 255)
        )
    }

    override fun getPreviewSize() = Pair(element.width * element.scale, element.height * element.scale)
}