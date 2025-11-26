package xyz.meowing.zen.utils

import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.client.renderer.debug.DebugRenderer
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.ClipContext
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object Render3D {
    private fun getValidLineWidth(width: Float): Float {
        val range = FloatArray(2)
        GL11.glGetFloatv(GL11.GL_LINE_WIDTH_RANGE, range)
        return min(max(width, range[0]), range[1])
    }

    fun drawEntityFilled(matrices: PoseStack?, vertexConsumers: MultiBufferSource?, x: Double, y: Double, z: Double, width: Float, height: Float, r: Float, g: Float, b: Float, a: Float) {
        val box = AABB(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2)
        DebugRenderer.renderFilledBox(
            matrices,
            vertexConsumers,
            box,
            r,
            g,
            b,
            a
        )
    }

    fun drawString(
        text: String,
        pos: Vec3,
        color: Int = -1,
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
        val camera = client.gameRenderer.mainCamera
        val cameraPos = camera.position
        val allocator = ByteBufferBuilder(256)
        val consumers = MultiBufferSource.immediate(allocator)

        val dirVec = Vec3(cameraPos.x - pos.x, 0.0, cameraPos.z - pos.z).normalize()
        val playerOffsetPos = Vec3(pos.x + dirVec.x * 0.5, pos.y, pos.z + dirVec.z * 0.5)

        val renderPos: Vec3
        val finalScale: Float

        if (dynamic) {
            val player = player ?: return
            val eyeHeight = player.eyeHeight
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

            renderPos = Vec3(resultX, resultY, resultZ)
        } else {
            renderPos = playerOffsetPos
            finalScale = scale
        }

        val lines = text.split("\n")
        val fontHeight = client.font.lineHeight.toFloat()
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
                .rotate(camera.rotation())
                .scale(finalScale * 0.025f, -(finalScale * 0.025f), finalScale * 0.025f)

            val xOffset = -client.font.width(line) / 2f
            client.font.drawInBatch(
                line,
                xOffset,
                0f,
                color,
                shadow,
                positionMatrix,
                consumers,
                if (depth) Font.DisplayMode.NORMAL else Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT
            )
        }
        consumers.endBatch()
    }

    fun drawLineToEntity(entity: Entity, consumers: MultiBufferSource?, matrixStack: PoseStack?, colorComponents: FloatArray, alpha: Float) {
        val player = player ?: return
        if (!player.hasLineOfSight(entity)) return

        val entityPos = entity.position().add(0.0, entity.eyeHeight.toDouble(), 0.0)
        drawLineToPos(entityPos, consumers, matrixStack, colorComponents, alpha)
    }

    fun drawLineToPos(pos: Vec3, consumers: MultiBufferSource?, matrixStack: PoseStack?, colorComponents: FloatArray, alpha: Float) {
        val player = player ?: return
        val playerPos = player.getEyePosition(Utils.partialTicks)
        val toTarget = pos.subtract(playerPos).normalize()
        val lookVec = player.getViewVector(Utils.partialTicks).normalize()

        if (toTarget.dot(lookVec) < 0.3) return

        val result = player.level().clip(ClipContext(playerPos, pos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player))
        if (result.type == HitResult.Type.BLOCK) return

        drawLineFromCursor(consumers, matrixStack, pos, colorComponents, alpha)
    }

    fun drawLine(start: Vec3, finish: Vec3, thickness: Float, color: Color, consumers: MultiBufferSource?, matrixStack: PoseStack?) {
        val cameraPos = client.gameRenderer.mainCamera.position
        val matrices = matrixStack ?: return
        matrices.pushPose()
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
        val entry = matrices.last()
        val consumers = consumers as MultiBufferSource.BufferSource
        val buffer = consumers.getBuffer(RenderType.lines())

        val validThickness = getValidLineWidth(thickness)
        GL11.glLineWidth(validThickness)

        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val a = color.alpha / 255f

        val direction = finish.subtract(start).normalize().toVector3f()

        buffer.addVertex(entry, start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
            .setColor(r, g, b, a)
            .setNormal(entry, direction)

        buffer.addVertex(entry, finish.x.toFloat(), finish.y.toFloat(), finish.z.toFloat())
            .setColor(r, g, b, a)
            .setNormal(entry, direction)

        consumers.endBatch(RenderType.lines())
        matrices.popPose()
    }

    fun drawLineFromCursor(consumers: MultiBufferSource?, matrixStack: PoseStack?, point: Vec3, colorComponents: FloatArray, alpha: Float) {
        val camera = client.gameRenderer.mainCamera
        val cameraPos = camera.position
        matrixStack?.pushPose()
        matrixStack?.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
        val entry = matrixStack?.last()
        val consumers = consumers as MultiBufferSource.BufferSource
        val layer = RenderType.lines()
        val buffer = consumers.getBuffer(layer)

        val cameraPoint = cameraPos.add(Vec3.directionFromRotation(camera.xRot, camera.yRot))
        val normal = point.toVector3f().sub(cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat()).normalize()

        buffer.addVertex(entry, cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat())
            .setColor(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
            .setNormal(entry, normal)

        buffer.addVertex(entry, point.x.toFloat(), point.y.toFloat(), point.z.toFloat())
            .setColor(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
            .setNormal(entry, normal)

        consumers.endBatch(layer)
        matrixStack?.popPose()
    }

    fun drawFilledCircle(consumers: MultiBufferSource?, matrixStack: PoseStack?, center: Vec3, radius: Float, segments: Int, borderColor: Int, fillColor: Int) {
        val camera = client.gameRenderer.mainCamera.position
        matrixStack?.pushPose()
        matrixStack?.translate(-camera.x, -camera.y, -camera.z)
        val entry = matrixStack?.last()
        val consumers = consumers as MultiBufferSource.BufferSource

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

        val triangleBuffer = consumers.getBuffer(RenderType.debugFilledBox())
        triangleBuffer.addVertex(entry, centerX, centerY, centerZ).setColor(fillR, fillG, fillB, fillA)

        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val x = centerX + radius * cos(angle).toFloat()
            val z = centerZ + radius * sin(angle).toFloat()
            triangleBuffer.addVertex(entry, x, centerY, z).setColor(fillR, fillG, fillB, fillA)

            if (i > 0) {
                val prevAngle = Math.PI * 2 * (i - 1) / segments
                val prevX = centerX + radius * cos(prevAngle).toFloat()
                val prevZ = centerZ + radius * sin(prevAngle).toFloat()

                triangleBuffer.addVertex(entry, centerX, centerY, centerZ).setColor(fillR, fillG, fillB, fillA)
                triangleBuffer.addVertex(entry, prevX, centerY, prevZ).setColor(fillR, fillG, fillB, fillA)
                triangleBuffer.addVertex(entry, x, centerY, z).setColor(fillR, fillG, fillB, fillA)
            }
        }

        val lineBuffer = consumers.getBuffer(RenderType.lines())
        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val nextAngle = Math.PI * 2 * ((i + 1) % segments) / segments

            val x1 = centerX + radius * cos(angle).toFloat()
            val z1 = centerZ + radius * sin(angle).toFloat()
            val x2 = centerX + radius * cos(nextAngle).toFloat()
            val z2 = centerZ + radius * sin(nextAngle).toFloat()

            val normal = Vec3((x2 - x1).toDouble(), 0.0, (z2 - z1).toDouble()).normalize().toVector3f()

            lineBuffer.addVertex(entry, x1, centerY, z1)
                .setColor(borderR, borderG, borderB, borderA)
                .setNormal(entry, normal)
            lineBuffer.addVertex(entry, x2, centerY, z2)
                .setColor(borderR, borderG, borderB, borderA)
                .setNormal(entry, normal)
        }

        consumers.endBatch()
        matrixStack?.popPose()
    }

    fun drawSpecialBB(pos: BlockPos, fillColor: Color, consumers: MultiBufferSource?, matrixStack: PoseStack?) {
        val bb = AABB(pos).inflate(0.002, 0.002, 0.002)
        drawSpecialBB(bb, fillColor, consumers, matrixStack)
    }

    fun drawSpecialBB(bb: AABB, fillColor: Color, consumers: MultiBufferSource?, matrixStack: PoseStack?) {
        drawFilledBB(bb, fillColor.withAlpha(0.6f), consumers, matrixStack)
        drawOutlinedBB(bb, fillColor.withAlpha(0.9f), consumers, matrixStack)
    }

    fun drawOutlinedBB(bb: AABB, color: Color, consumers: MultiBufferSource?, matrixStack: PoseStack?) {
        val camera = client.gameRenderer.mainCamera.position
        val matrices = matrixStack ?: return
        matrices.pushPose()
        matrices.translate(-camera.x, -camera.y, -camera.z)
        val consumers = consumers as MultiBufferSource.BufferSource
        val buffer = consumers.getBuffer(RenderType.lines())

        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val a = color.alpha / 255f

        ShapeRenderer.renderLineBox(
            //#if MC >= 1.21.9
            //$$ matrices.last(),
            //#else
            matrices,
            //#endif
            buffer,
            bb.minX,
            bb.minY,
            bb.minZ,
            bb.maxX,
            bb.maxY,
            bb.maxZ,
            r,
            g,
            b,
            a
        )

        consumers.endBatch(RenderType.lines())
        matrices.popPose()
    }

    fun drawFilledBB(bb: AABB, color: Color, consumers: MultiBufferSource?, matrixStack: PoseStack?) {
        val aabb = bb.inflate(0.001, 0.001, 0.001)
        val camera = client.gameRenderer.mainCamera.position
        val matrices = matrixStack ?: return
        matrices.pushPose()
        matrices.translate(-camera.x, -camera.y, -camera.z)
        val entry = matrices.last()
        val consumers = consumers as MultiBufferSource.BufferSource
        val buffer = consumers.getBuffer(RenderType.debugFilledBox())

        val a = color.alpha / 255f
        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f

        val minX = aabb.minX.toFloat()
        val minY = aabb.minY.toFloat()
        val minZ = aabb.minZ.toFloat()
        val maxX = aabb.maxX.toFloat()
        val maxY = aabb.maxY.toFloat()
        val maxZ = aabb.maxZ.toFloat()

        buffer.addVertex(entry, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, minX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(entry, minX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, minX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(entry, minX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(entry, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, minX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(entry, maxX, minY, maxZ).setColor(r, g, b, a)

        consumers.endBatch(RenderType.debugFilledBox())
        matrices.popPose()
    }

    fun drawFilledShapeVoxel(shape: VoxelShape, color: Color, consumers: MultiBufferSource?, matrixStack: PoseStack?) {
        shape.toAabbs().forEach { box ->
            drawFilledBB(
                box,
                color,
                consumers,
                matrixStack
            )
        }
    }

    private fun Color.withAlpha(alpha: Float) = Color(red, green, blue, (alpha * 255).toInt())
}