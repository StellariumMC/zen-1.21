package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.BlockModelRenderer
import net.minecraft.client.render.model.BlockStateManagers
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d

@Zen.Module
object Rat : Feature(area = "Hub") {
    private val position = Vec3d(-1.0, 72.0, -92.0)
    private val culling = Box(position.x, position.y, position.z, position.x + 1, position.y + 1, position.z + 1/16.0)
    private val texture = Identifier.of("zen", "rat.png")

    override fun initialize() {
        register<RenderEvent.WorldPostEntities> { event ->
            render(event.context!!)
        }
    }

    private fun render(context: WorldRenderContext) {
        val cameraPos = context.camera().pos
        val frustum = context.frustum()

        if (position.distanceTo(cameraPos) > 96.0) return
        if (frustum != null && !frustum.isVisible(culling)) return

        val itemFrameState = BlockStateManagers.getStateForItemFrame(false, true)
        val blockModel = mc.blockRenderManager.getModel(itemFrameState)
        val consumers = context.consumers() ?: return
        val matrices = context.matrixStack() ?: return

        matrices.push()
        matrices.translate(
            position.x - cameraPos.x + 1.0,
            position.y - cameraPos.y,
            position.z - cameraPos.z + 1.0
        )
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))

        val entry = matrices.peek()
        val blockBuffer = consumers.getBuffer(
            RenderLayer.getEntitySolidZOffsetForward(Identifier.of("minecraft", "textures/atlas/blocks.png"))
        )

        BlockModelRenderer.render(
            entry,
            blockBuffer,
            blockModel,
            1.0f, 1.0f, 1.0f,
            15,
            OverlayTexture.DEFAULT_UV
        )

        matrices.translate(1.0f, 1.0f, 0.0f)
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f))

        val posMatrix = entry.positionMatrix
        val overlayBuffer = consumers.getBuffer(RenderLayer.getText(texture))
        val depth = 0.9375f - 0.000488f

        overlayBuffer.vertex(posMatrix, 0f, 1f, depth).color(-1).texture(0f, 1f).light(15)
        overlayBuffer.vertex(posMatrix, 1f, 1f, depth).color(-1).texture(1f, 1f).light(15)
        overlayBuffer.vertex(posMatrix, 1f, 0f, depth).color(-1).texture(1f, 0f).light(15)
        overlayBuffer.vertex(posMatrix, 0f, 0f, depth).color(-1).texture(0f, 0f).light(15)

        matrices.pop()
    }
}