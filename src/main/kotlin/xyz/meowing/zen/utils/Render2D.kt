package xyz.meowing.zen.utils

import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraft.util.CommonColors
import xyz.meowing.knit.api.KnitClient.client

object Render2D {
    enum class TextStyle {
        DROP_SHADOW,
        DEFAULT
    }

    fun renderString(
        context: GuiGraphics,
        text: String,
        x: Float,
        y: Float,
        scale: Float,
        colors: Int = CommonColors.WHITE,
        textStyle: TextStyle = TextStyle.DEFAULT
    ) {
        context.pushPop {
            //#if MC >= 1.21.7
            //$$ context.pose().translate(x, y)
            //$$ context.pose().scale(scale, scale)
            //#else
            context.pose().translate(x, y, 0f)
            context.pose().scale(scale, scale, 1f)
            //#endif

            when (textStyle) {
                TextStyle.DROP_SHADOW -> {
                    context.drawString(client.font, text, 0, 0, colors, true)
                }

                TextStyle.DEFAULT -> {
                    context.drawString(client.font, text, 0, 0, colors, false)
                }
            }
        }
    }

    fun renderStringWithShadow(context: GuiGraphics, text: String, x: Float, y: Float, scale: Float, colors: Int = CommonColors.WHITE) {
        renderString(context, text, x, y, scale, colors, TextStyle.DROP_SHADOW)
    }

    fun renderItem(context: GuiGraphics, item: ItemStack, x: Float, y: Float, scale: Float) {
        context.pushPop {
            //#if MC >= 1.21.7
            //$$ context.pose().translate(x, y)
            //$$ context.pose().scale(scale, scale)
            //#else
            context.pose().translate(x, y, 0f)
            context.pose().scale(scale, scale, 1f)
            //#endif

            context.renderItem(item, 0, 0)
        }
    }

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { client.font.width(it.removeFormatting()) }
    }

    fun String.height(): Int {
        val lineCount = count { it == '\n' } + 1
        return client.font.lineHeight * lineCount
    }

    inline fun GuiGraphics.pushPop(block: () -> Unit) {
        //#if MC >= 1.21.7
        //$$ pose().pushMatrix()
        //#else
        pose().pushPose()
        //#endif
        block()
        //#if MC >= 1.21.7
        //$$ pose().popMatrix()
        //#else
        pose().popPose()
        //#endif
    }

    fun GuiGraphics.renderOutline(i: Int, j: Int, k: Int, l: Int, m: Int) {
        this.fill(i, j, i + k, j + 1, m);
        this.fill(i, j + l - 1, i + k, j + l, m);
        this.fill(i, j + 1, i + 1, j + l - 1, m);
        this.fill(i + k - 1, j + 1, i + k, j + l - 1, m);
    }
}