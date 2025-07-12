package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
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
    fun drawLineToEntity(entity: Entity, context: WorldRenderContext, colorComponents: FloatArray, alpha: Float) {
        val player = mc.player ?: return
        if (!player.canSee(entity)) return

        val entityPos = entity.pos.add(0.0, entity.standingEyeHeight.toDouble(), 0.0)
        drawLineToPos(entityPos, context, colorComponents, alpha)
    }

    fun drawLineToPos(pos: Vec3d, context: WorldRenderContext, colorComponents: FloatArray, alpha: Float) {
        val player = mc.player ?: return
        val playerPos = player.getCameraPosVec(context.tickCounter().getTickProgress(false))
        val toTarget = pos.subtract(playerPos).normalize()
        val lookVec = player.getRotationVec(context.tickCounter().getTickProgress(false)).normalize()

        if (toTarget.dotProduct(lookVec) < 0.3) return

        val result = player.world.raycast(RaycastContext(playerPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player))
        if (result.type == HitResult.Type.BLOCK) return

        renderLineFromCursor(context, pos, colorComponents, alpha)
    }

    fun renderLineFromCursor(context: WorldRenderContext, point: Vec3d, colorComponents: FloatArray, alpha: Float) {
        val camera = context.camera().pos
        val matrices = context.matrixStack()
        matrices?.push()
        matrices?.translate(-camera.x, -camera.y, -camera.z)
        val entry = matrices?.peek()
        val consumers = context.consumers() as VertexConsumerProvider.Immediate
        val layer = RenderLayer.getLines()
        val buffer = consumers.getBuffer(layer)

        val cameraPoint = camera.add(Vec3d.fromPolar(context.camera().pitch, context.camera().yaw))
        val normal = point.toVector3f().sub(cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat()).normalize()

        buffer.vertex(entry, cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat())
            .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
            .normal(entry, normal)

        buffer.vertex(entry, point.x.toFloat(), point.y.toFloat(), point.z.toFloat())
            .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
            .normal(entry, normal)

        consumers.draw(layer)
        matrices?.pop()
    }
}