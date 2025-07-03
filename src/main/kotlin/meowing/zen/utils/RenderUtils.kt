package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import java.awt.Color

object RenderUtils {
    fun renderEntityFilled(
        matrices: MatrixStack?,
        vertexConsumers:
        VertexConsumerProvider?,
        x: Double,
        y: Double,
        z: Double,
        width: Float,
        height: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        val box = Box(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2)
        DebugRenderer.drawBox(
            matrices,
            vertexConsumers,
            box,
            r,
            g,
            b,
            a
        )
    }

    fun renderEntityOutline(
        matrices: MatrixStack?,
        vertex: VertexConsumerProvider?,
        x: Double,
        y: Double,
        z: Double,
        width: Double,
        height: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        val halfWidth = width / 2
        VertexRendering.drawBox(
            matrices,
            vertex!!.getBuffer(RenderLayer.getLines()),
            x - halfWidth,
            y,
            z - halfWidth,
            x + halfWidth,
            y + height,
            z + halfWidth,
            r,
            g,
            b,
            a
        )
    }

    fun renderText(
        text: String,
        pos: Vec3d,
        color: Int,
        scale: Float = 1.0f,
        yOffset: Float = 0.0f,
        throughWalls: Boolean = false
    ) {
        val lines = text.split('\n')
        val camera = mc.gameRenderer.camera
        val cameraPos = camera.pos
        val textRenderer = mc.textRenderer
        val adjustedScale = scale * 0.025f

        val allocator = BufferAllocator(256)
        val consumers = VertexConsumerProvider.immediate(allocator)

        lines.forEachIndexed { i, line ->
            val positionMatrix = Matrix4f()
                .translate(
                    (pos.x - cameraPos.x).toFloat(),
                    (pos.y - cameraPos.y).toFloat(),
                    (pos.z - cameraPos.z).toFloat()
                )
                .rotate(camera.rotation)
                .scale(adjustedScale, -adjustedScale, adjustedScale)

            val xOffset = -textRenderer.getWidth(line) / 2f
            val lineYOffset = yOffset + i * 10f

            textRenderer.draw(
                line,
                xOffset,
                lineYOffset,
                color,
                false,
                positionMatrix,
                consumers,
                if (throughWalls) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
            )
        }

        consumers.draw()
    }
}