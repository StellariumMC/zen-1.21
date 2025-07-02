package meowing.zen.utils

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box

object RenderUtils {
    fun renderEntityFilled(matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Float, height: Float, r: Float, g: Float, b: Float, a: Float) {
        val box = Box(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2)
        DebugRenderer.drawBox(matrices, vertexConsumers, box, r, g, b, a)
    }

    fun renderEntityOutline(matrices: MatrixStack?, vertex: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Double, height: Float, r: Float, g: Float, b: Float, a: Float) {
        val halfWidth = width / 2
        VertexRendering.drawBox(
            matrices,
            vertex!!.getBuffer(RenderLayer.getLines()),
            x - halfWidth, y, z - halfWidth, x + halfWidth, y + height, z + halfWidth,
            r, g, b, a
        )
    }
}