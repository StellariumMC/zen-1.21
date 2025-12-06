package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.features.Feature
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.world.level.EmptyBlockGetter
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.annotations.Module
import java.awt.Color
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.events.core.RenderEvent

@Module
object BlockOverlay : Feature(
    "blockOverlay",
    "Block overlay",
    "Customizes block selection overlay",
    "Visuals"
) {
    private val overlayColor by config.colorPicker("Fill color", Color(0, 255, 255, 38))
    private val borderColor by config.colorPicker("Border color")
    private val filled by config.switch("Filled overlay")
    private val bordered by config.switch("Bordered overlay", true)

    override fun initialize() {
        register<RenderEvent.World.BlockOutline> { event ->
            val blockPos = event.context.blockPos() ?: return@register
            val blockState = event.context.blockState() ?: return@register
            val matrixStack = event.context.matrixStack() ?: return@register

            val consumers = event.context.consumers()
            val camera = client.gameRenderer.mainCamera
            val blockShape = blockState.getShape(EmptyBlockGetter.INSTANCE, blockPos, CollisionContext.of(camera.entity))

            if (blockShape.isEmpty) return@register

            val camPos = camera.position
            event.cancel()

            if (bordered) {
                ShapeRenderer.renderShape(
                    matrixStack,
                    consumers.getBuffer(RenderType.lines()),
                    blockShape,
                    blockPos.x - camPos.x,
                    blockPos.y - camPos.y,
                    blockPos.z - camPos.z,
                    borderColor.rgb
                )
            }

            if (filled) {
                Render3D.drawFilledShapeVoxel(
                    blockShape.move(blockPos),
                    overlayColor,
                    consumers,
                    matrixStack
                )
            }
        }
    }
}
