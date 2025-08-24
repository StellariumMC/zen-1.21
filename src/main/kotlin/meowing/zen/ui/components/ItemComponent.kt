package meowing.zen.ui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UMatrixStack
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import java.awt.Color

class ItemComponent(var stack: ItemStack, var resolution: Float = 16f) : UIBlock() {
    override fun draw(matrixStack: UMatrixStack) {
        val GlStateManager = UMatrixStack.UNIT
        GlStateManager.push()
        GlStateManager.translate(getLeft(), getTop(), 0f)
        GlStateManager.scale(resolution / 16f, resolution / 16f, 1f)
        GlStateManager.runWithGlobalState {
            Render2D.renderItem(DrawContext(mc, mc.bufferBuilders.effectVertexConsumers), stack, 0f, 0f, 1f)
        }
        GlStateManager.pop()
    }

    init {
        this.constrain {
            color = Color(0, 0, 0, 0).constraint
            width = resolution.pixels
            height = resolution.pixels
        }
    }
}