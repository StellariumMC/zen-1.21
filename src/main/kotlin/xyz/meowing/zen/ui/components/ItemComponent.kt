package xyz.meowing.zen.ui.components

import gg.essential.elementa.UIComponent
import gg.essential.universal.UMatrixStack
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.item.ItemRenderState
import net.minecraft.item.ItemDisplayContext
import net.minecraft.item.ItemStack
import xyz.meowing.knit.api.KnitClient.client

class ItemComponent(var stack: ItemStack, var resolution: Float = 16f) : UIComponent() {
    override fun draw(matrixStack: UMatrixStack) {
        beforeDraw(matrixStack)

        val matrices = matrixStack.toMC()
        val renderState = ItemRenderState()
        val vertexConsumers = client.bufferBuilders.entityVertexConsumers

        if (!stack.isEmpty) {
            client.itemModelManager.clearAndUpdate(renderState, stack, ItemDisplayContext.GUI, client.world, client.player, 0)
            matrices.push()
            val x = getLeft()
            val y = getTop()
            matrices.translate((x + resolution / 2), (y + resolution / 2), 100f)

            matrices.scale(resolution, -resolution, resolution)
//            val isSideLit: Boolean = renderState.isSideLit
//
//            if (isSideLit) {
//                vertexConsumers.draw()
//                DiffuseLighting.disableGuiDepthLighting()
//            }

            renderState.render(matrices, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV)
            vertexConsumers.draw()

//            if (isSideLit) {
//                DiffuseLighting.enableGuiDepthLighting()
//            }

            matrices.pop()
        }

        super.draw(matrixStack)
    }
}