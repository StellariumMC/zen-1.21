package meowing.zen.utils

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box

object RenderUtils {
    fun renderEntityBox(matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Float, height: Float, r: Float, g: Float, b: Float, a: Float) {
        val box = Box(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2)
        DebugRenderer.drawBox(matrices, vertexConsumers, box, r, g, b, a)
    }
}