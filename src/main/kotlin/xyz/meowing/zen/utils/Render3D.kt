package xyz.meowing.zen.utils

import xyz.meowing.zen.Zen.Companion.mc
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
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

//#if MC < 1.21.9
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
//#endif

object Render3D {
    private fun getValidLineWidth(width: Float): Float {
        val range = FloatArray(2)
        GL11.glGetFloatv(GL11.GL_LINE_WIDTH_RANGE, range)
        return min(max(width, range[0]), range[1])
    }

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

    //#if MC < 1.21.9
    fun drawEntityOutline(matrices: MatrixStack?, vertex: VertexConsumerProvider?, x: Double, y: Double, z: Double, width: Double, height: Float, r: Float, g: Float, b: Float, a: Float) {
        val halfWidth = width / 2
        VertexRendering.drawBox(
            //#if MC >= 1.21.9
            //$$ matrices?.peek()
            //#else
            matrices,
            //#endif
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
    //#endif

    fun drawString(
        text: String,
        pos: Vec3d,
        color: Int = 0xFFFFFF,
        scale: Float = 1.0f,
        yOffset: Float = 0.0f,
        depth: Boolean = true,
        dynamic: Boolean = true,
        scaleMultiplier: Double = 1.0,
        hideTooCloseAt: Double = 4.5,
        smallestDistanceView: Double = 5.0,
        maxDistance: Int? = null,
        ignoreY: Boolean = false,
        shadow: Boolean = true
    ) {
        val camera = mc.gameRenderer.camera
        val cameraPos = camera.pos
        val allocator = BufferAllocator(256)
        val consumers = VertexConsumerProvider.immediate(allocator)

        val dirVec = Vec3d(cameraPos.x - pos.x, 0.0, cameraPos.z - pos.z).normalize()
        val playerOffsetPos = Vec3d(pos.x + dirVec.x * 0.5, pos.y, pos.z + dirVec.z * 0.5)

        val renderPos: Vec3d
        val finalScale: Float

        if (dynamic) {
            val player = mc.player ?: return
            val eyeHeight = player.standingEyeHeight
            val x = playerOffsetPos.x
            val y = playerOffsetPos.y
            val z = playerOffsetPos.z

            val dX = (x - cameraPos.x) * (x - cameraPos.x)
            val dY = (y - (cameraPos.y + eyeHeight)) * (y - (cameraPos.y + eyeHeight))
            val dZ = (z - cameraPos.z) * (z - cameraPos.z)
            val distToPlayerSq = dX + dY + dZ
            var distToPlayer = sqrt(distToPlayerSq)

            distToPlayer = distToPlayer.coerceAtLeast(smallestDistanceView)
            if (distToPlayer < hideTooCloseAt) return
            maxDistance?.let { if (!depth && distToPlayer > it) return }

            val distRender = distToPlayer.coerceAtMost(50.0)
            val dynamicScale = (distRender / 12) * scaleMultiplier
            finalScale = dynamicScale.toFloat()

            val resultX = cameraPos.x + (x - cameraPos.x) / (distToPlayer / distRender)
            val resultY = if (ignoreY) y * distToPlayer / distRender
            else cameraPos.y + eyeHeight + (y + 20 * distToPlayer / 300 - (cameraPos.y + eyeHeight)) / (distToPlayer / distRender)
            val resultZ = cameraPos.z + (z - cameraPos.z) / (distToPlayer / distRender)

            renderPos = Vec3d(resultX, resultY, resultZ)
        } else {
            renderPos = playerOffsetPos
            finalScale = scale
        }

        val lines = text.split("\n")
        val fontHeight = mc.textRenderer.fontHeight.toFloat()
        val scaledFontHeight = fontHeight * finalScale * 0.025f
        val totalHeight = lines.size * scaledFontHeight
        val startY = -(totalHeight / 2f) + yOffset

        lines.forEachIndexed { index, line ->
            val lineY = startY + (index * scaledFontHeight)
            val positionMatrix = Matrix4f()
                .translate(
                    (renderPos.x - cameraPos.x).toFloat(),
                    (renderPos.y - cameraPos.y + lineY).toFloat(),
                    (renderPos.z - cameraPos.z).toFloat()
                )
                .rotate(camera.rotation)
                .scale(finalScale * 0.025f, -(finalScale * 0.025f), finalScale * 0.025f)

            val xOffset = -mc.textRenderer.getWidth(line) / 2f
            mc.textRenderer.draw(
                line,
                xOffset,
                0f,
                color,
                shadow,
                positionMatrix,
                consumers,
                if (depth) TextRenderer.TextLayerType.NORMAL else TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
            )
        }
        consumers.draw()
    }

    //#if MC < 1.21.9
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

        val validThickness = getValidLineWidth(thickness)
        GL11.glLineWidth(validThickness)

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
        drawFilledBB(bb, fillColor.withAlpha(0.6f), context)
        drawOutlinedBB(bb, fillColor.withAlpha(0.9f), context)
    }

    fun drawOutlinedBB(bb: Box, color: Color, context: WorldRenderContext) {
        val camera = context.camera().pos
        val matrices = context.matrixStack() ?: return
        matrices.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)
        val consumers = context.consumers() as VertexConsumerProvider.Immediate
        val buffer = consumers.getBuffer(RenderLayer.getLines())

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
    //#endif

    private fun Color.withAlpha(alpha: Float) = Color(red, green, blue, (alpha * 255).toInt())
}