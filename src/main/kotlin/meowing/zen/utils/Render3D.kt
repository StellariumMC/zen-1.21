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
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object Render3D {
    fun drawEntityFilled(matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Float, height: Float, r: Float, g: Float, b: Float, a: Float) {
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

    fun drawEntityOutline(matrices: MatrixStack?, vertex: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Double, height: Float, r: Float, g: Float, b: Float, a: Float) {
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

    fun drawString(text: String, pos: Vec3d, color: Int, scale: Float = 1.0f, yOffset: Float = 0.0f) {
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

        drawLineFromCursor(context, pos, colorComponents, alpha)
    }

    fun drawLine(start: Vec3d, finish: Vec3d, thickness: Float, color: Color, context: WorldRenderContext) {
        val camera = context.camera().pos
        val matrices = context.matrixStack() ?: return
        matrices.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)
        val entry = matrices.peek()
        val consumers = context.consumers() as VertexConsumerProvider.Immediate
        val buffer = consumers.getBuffer(RenderLayer.getLines())

        GL11.glLineWidth(thickness)

        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val a = color.alpha / 255f

        val direction = finish.subtract(start).normalize().toVector3f()

        buffer.vertex(entry, start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
            .color(r, g, b, a)
            .normal(entry, direction)

        buffer.vertex(entry, finish.x.toFloat(), finish.y.toFloat(), finish.z.toFloat())
            .color(r, g, b, a)
            .normal(entry, direction)

        consumers.draw(RenderLayer.getLines())
        matrices.pop()
    }

    fun drawLineFromCursor(context: WorldRenderContext, point: Vec3d, colorComponents: FloatArray, alpha: Float) {
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

    fun drawFilledCircle(context: WorldRenderContext, center: Vec3d, radius: Float, segments: Int, borderColor: Int, fillColor: Int) {
        val camera = context.camera().pos
        val matrices = context.matrixStack()
        matrices?.push()
        matrices?.translate(-camera.x, -camera.y, -camera.z)
        val entry = matrices?.peek()
        val consumers = context.consumers() as VertexConsumerProvider.Immediate

        val centerX = center.x.toFloat()
        val centerY = center.y.toFloat() + 0.01f
        val centerZ = center.z.toFloat()

        val fillR = ((fillColor shr 16) and 0xFF) / 255f
        val fillG = ((fillColor shr 8) and 0xFF) / 255f
        val fillB = (fillColor and 0xFF) / 255f
        val fillA = ((fillColor shr 24) and 0xFF) / 255f

        val borderR = ((borderColor shr 16) and 0xFF) / 255f
        val borderG = ((borderColor shr 8) and 0xFF) / 255f
        val borderB = (borderColor and 0xFF) / 255f
        val borderA = ((borderColor shr 24) and 0xFF) / 255f

        val triangleBuffer = consumers.getBuffer(RenderLayer.getDebugFilledBox())
        triangleBuffer.vertex(entry, centerX, centerY, centerZ).color(fillR, fillG, fillB, fillA)

        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val x = centerX + radius * cos(angle).toFloat()
            val z = centerZ + radius * sin(angle).toFloat()
            triangleBuffer.vertex(entry, x, centerY, z).color(fillR, fillG, fillB, fillA)

            if (i > 0) {
                val prevAngle = Math.PI * 2 * (i - 1) / segments
                val prevX = centerX + radius * cos(prevAngle).toFloat()
                val prevZ = centerZ + radius * sin(prevAngle).toFloat()

                triangleBuffer.vertex(entry, centerX, centerY, centerZ).color(fillR, fillG, fillB, fillA)
                triangleBuffer.vertex(entry, prevX, centerY, prevZ).color(fillR, fillG, fillB, fillA)
                triangleBuffer.vertex(entry, x, centerY, z).color(fillR, fillG, fillB, fillA)
            }
        }

        val lineBuffer = consumers.getBuffer(RenderLayer.getLines())
        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val nextAngle = Math.PI * 2 * ((i + 1) % segments) / segments

            val x1 = centerX + radius * cos(angle).toFloat()
            val z1 = centerZ + radius * sin(angle).toFloat()
            val x2 = centerX + radius * cos(nextAngle).toFloat()
            val z2 = centerZ + radius * sin(nextAngle).toFloat()

            val normal = Vec3d((x2 - x1).toDouble(), 0.0, (z2 - z1).toDouble()).normalize().toVector3f()

            lineBuffer.vertex(entry, x1, centerY, z1)
                .color(borderR, borderG, borderB, borderA)
                .normal(entry, normal)
            lineBuffer.vertex(entry, x2, centerY, z2)
                .color(borderR, borderG, borderB, borderA)
                .normal(entry, normal)
        }

        consumers.draw()
        matrices?.pop()
    }

    fun drawSpecialBB(pos: BlockPos, fillColor: Color, context: WorldRenderContext) {
        val bb = Box(pos).expand(0.002, 0.002, 0.002)
        drawSpecialBB(bb, fillColor, context)
    }

    fun drawSpecialBB(bb: Box, fillColor: Color, context: WorldRenderContext) {
        val player = mc.player ?: return
        val width = max(1.0 - (player.distanceTo(Vec3d(bb.minX, bb.minY, bb.minZ)) / 10 - 2), 2.0).toFloat()
        drawFilledBB(bb, fillColor.withAlpha(0.6f), context)
        drawOutlinedBB(bb, fillColor.withAlpha(0.9f), width, context)
    }

    fun drawOutlinedBB(bb: Box, color: Color, width: Float, context: WorldRenderContext) {
        val camera = context.camera().pos
        val matrices = context.matrixStack() ?: return
        matrices.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)
        val consumers = context.consumers() as VertexConsumerProvider.Immediate
        val buffer = consumers.getBuffer(RenderLayer.getLines())

        GL11.glLineWidth(width)

        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val a = color.alpha / 255f

        VertexRendering.drawBox(
            matrices, buffer,
            bb.minX, bb.minY, bb.minZ,
            bb.maxX, bb.maxY, bb.maxZ,
            r, g, b, a
        )

        consumers.draw(RenderLayer.getLines())
        matrices.pop()
    }

    fun drawFilledBB(bb: Box, color: Color, context: WorldRenderContext, customAlpha: Float = 0.15f) {
        val aabb = bb.expand(0.004, 0.005, 0.004)
        val camera = context.camera().pos
        val matrices = context.matrixStack() ?: return
        matrices.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)
        val entry = matrices.peek()
        val consumers = context.consumers() as VertexConsumerProvider.Immediate
        val buffer = consumers.getBuffer(RenderLayer.getDebugFilledBox())

        val a = (color.alpha / 255f * customAlpha)
        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f

        val minX = aabb.minX.toFloat()
        val minY = aabb.minY.toFloat()
        val minZ = aabb.minZ.toFloat()
        val maxX = aabb.maxX.toFloat()
        val maxY = aabb.maxY.toFloat()
        val maxZ = aabb.maxZ.toFloat()

        buffer.vertex(entry, minX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, maxZ).color(r, g, b, a)

        buffer.vertex(entry, maxX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, maxZ).color(r, g, b, a)

        buffer.vertex(entry, minX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, minZ).color(r, g, b, a)

        buffer.vertex(entry, minX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, minZ).color(r, g, b, a)

        buffer.vertex(entry, minX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, minZ).color(r, g, b, a)

        buffer.vertex(entry, minX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, minX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, minX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, minZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, minY, maxZ).color(r, g, b, a)
        buffer.vertex(entry, maxX, maxY, maxZ).color(r, g, b, a)

        consumers.draw(RenderLayer.getDebugFilledBox())
        matrices.pop()
    }

    private fun Color.withAlpha(alpha: Float) = Color(red, green, blue, (alpha * 255).toInt())

    private fun Entity.distanceTo(pos: Vec3d) = this.pos.distanceTo(pos).toFloat()
}