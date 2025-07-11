package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11

object RenderUtils {
    fun renderEntityFilled(
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
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

    fun drawString(
        text: String,
        pos: Vec3d,
        color: Int,
        scale: Float = 1.0f,
        yOffset: Float = 0.0f
    ) {
        val camera = mc.gameRenderer.camera
        val cameraPos = camera.pos
        val allocator = BufferAllocator(256)
        val consumers = VertexConsumerProvider.immediate(allocator)

        val positionMatrix = Matrix4f()
            .translate(
                (pos.x - cameraPos.x).toFloat(),
                (pos.y - cameraPos.y + yOffset).toFloat(),
                (pos.z - cameraPos.z).toFloat()
            )
            .rotate(camera.rotation)
            .scale(scale * 0.025f, -scale * 0.025f, scale * 0.025f)

        val xOffset = -mc.textRenderer.getWidth(text) / 2f
        val depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC)
        val depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_ALWAYS)

        mc.textRenderer.draw(
            text,
            xOffset,
            0f,
            color,
            false,
            positionMatrix,
            consumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )

        consumers.draw()

        GL11.glDepthFunc(depthFunc)
        if (!depthTest) GL11.glDisable(GL11.GL_DEPTH_TEST)
    }
}