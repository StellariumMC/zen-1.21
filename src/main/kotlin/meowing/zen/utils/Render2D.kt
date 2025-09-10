package meowing.zen.utils

import me.x150.renderer.render.ExtendedDrawContext
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Colors
import org.joml.Vector4f
import java.awt.Color

object Render2D {
    enum class TextStyle {
        DROP_SHADOW,
        BLACK_OUTLINE,
        DEFAULT
    }

    fun renderString(
        context: DrawContext,
        text: String,
        x: Float,
        y: Float,
        scale: Float,
        colors: Int = 0xFFFFFF,
        textStyle: TextStyle = TextStyle.DEFAULT
    ) {
        //#if MC >= 1.21.7
        //$$ context.matrices.pushMatrix()
        //$$ context.matrices.translate(x, y)
        //$$ context.matrices.scale(scale, scale)
        //#else
        context.matrices.push()
        context.matrices.translate(x, y, 0f)
        context.matrices.scale(scale, scale, 1f)
        //#endif

        when (textStyle) {
            TextStyle.DROP_SHADOW -> {
                context.drawText(mc.textRenderer, text, 0, 0, colors, true)
            }
            TextStyle.BLACK_OUTLINE -> {
                val matrices = context.matrices

                //#if MC >= 1.21.7
                //$$ matrices.pushMatrix()
                //$$ matrices.translate(-0.75f, 0f)
                //#else
                matrices.push()
                matrices.translate(-0.75f, 0f, 0f)
                //#endif
                context.drawText(mc.textRenderer, text.removeFormatting(), 0, 0, 0x000000, false)
                //#if MC >= 1.21.7
                //$$ context.matrices.popMatrix()
                //#else
                context.matrices.pop()
                //#endif

                //#if MC >= 1.21.7
                //$$ matrices.pushMatrix()
                //$$ matrices.translate(0.75f, 0f)
                //#else
                matrices.push()
                matrices.translate(0.75f, 0f, 0f)
                //#endif
                context.drawText(mc.textRenderer, text.removeFormatting(), 0, 0, 0x000000, false)
                //#if MC >= 1.21.7
                //$$ context.matrices.popMatrix()
                //#else
                context.matrices.pop()
                //#endif

                //#if MC >= 1.21.7
                //$$ matrices.pushMatrix()
                //$$ matrices.translate(0f, -0.75f)
                //#else
                matrices.push()
                matrices.translate(0f, -0.75f, 0f)
                //#endif
                context.drawText(mc.textRenderer, text.removeFormatting(), 0, 0, 0x000000, false)
                //#if MC >= 1.21.7
                //$$ context.matrices.popMatrix()
                //#else
                context.matrices.pop()
                //#endif

                //#if MC >= 1.21.7
                //$$ matrices.pushMatrix()
                //$$ matrices.translate(0f, 0.75f)
                //#else
                matrices.push()
                matrices.translate(0f, 0.75f, 0f)
                //#endif
                context.drawText(mc.textRenderer, text.removeFormatting(), 0, 0, 0x000000, false)
                //#if MC >= 1.21.7
                //$$ context.matrices.popMatrix()
                //#else
                context.matrices.pop()
                //#endif

                context.drawText(mc.textRenderer, text, 0, 0, colors, false)
            }
            TextStyle.DEFAULT -> {
                context.drawText(mc.textRenderer, text, 0, 0, colors, false)
            }
        }

        //#if MC >= 1.21.7
        //$$ context.matrices.popMatrix()
        //#else
        context.matrices.pop()
        //#endif
    }

    fun renderStringWithShadow(context: DrawContext, text: String, x: Float, y: Float, scale: Float, colors: Int = Colors.WHITE) {
        renderString(context, text, x, y, scale, colors, TextStyle.DROP_SHADOW)
    }

    fun renderItem(context: DrawContext, item: ItemStack, x: Float, y: Float, scale: Float) {
        //#if MC >= 1.21.7
        //$$ context.matrices.pushMatrix()
        //$$ context.matrices.translate(x, y)
        //$$ context.matrices.scale(scale, scale)
        //#else
        context.matrices.push()
        context.matrices.translate(x, y, 0f)
        context.matrices.scale(scale, scale, 1f)
        //#endif

        context.drawItem(item, 0, 0)

        //#if MC >= 1.21.7
        //$$ context.matrices.popMatrix()
        //#else
        context.matrices.pop()
        //#endif
    }

    // Renderer uses Colors in ABGR format
    fun renderRoundedRect(context: DrawContext, x: Float, y: Float, width: Float, height: Float, roundness: Vector4f, color: Color) {
        val renderColor = me.x150.renderer.util.Color(color.toABGR())
        ExtendedDrawContext.drawRoundedRect(context, x, y, width, height, roundness, renderColor)
    }

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { mc.textRenderer.getWidth(it.removeFormatting()) }
    }

    fun String.height(): Int {
        val lineCount = count { it == '\n' } + 1
        return mc.textRenderer.fontHeight * lineCount
    }

    fun Color.toABGR(): Color {
        return Color(blue, green, red, alpha)
    }
}