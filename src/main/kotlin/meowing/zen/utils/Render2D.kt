package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Colors

object Render2D {

    fun renderString(context: DrawContext, text: String, x: Float, y: Float, scale: Float, colors: Int = Colors.WHITE, shadow: Boolean = false) {
        //#if MC >= 1.21.7
        //$$ context.matrices.pushMatrix()
        //$$ context.matrices.translate(x, y)
        //$$ context.matrices.scale(scale, scale)
        //#else
        context.matrices.push()
        context.matrices.translate(x, y, 0f)
        context.matrices.scale(scale, scale, 1f)
        //#endif

        context.drawText(mc.textRenderer, text, 0, 0, colors, shadow)

        //#if MC >= 1.21.7
        //$$ context.matrices.popMatrix()
        //#else
        context.matrices.pop()
        //#endif
    }

    fun renderStringWithShadow(context: DrawContext, text: String, x: Float, y: Float, scale: Float, colors: Int = Colors.WHITE) {
        renderString(context, text, x, y, scale, colors, true)
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

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { mc.textRenderer.getWidth(it.removeFormatting()) }
    }

    fun String.height(): Int {
        val lineCount = count { it == '\n' } + 1
        return mc.textRenderer.fontHeight * lineCount
    }
}