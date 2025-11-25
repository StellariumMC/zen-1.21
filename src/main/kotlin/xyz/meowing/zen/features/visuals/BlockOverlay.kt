package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
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
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object BlockOverlay : Feature(
    "blockOverlay"
) {
    private val blockOverlayColor by ConfigDelegate<Color>("blockOverlay.overlayColor")
    private val blockBorderColor by ConfigDelegate<Color>("blockOverlay.borderColor")
    private val blockOverlayFilled by ConfigDelegate<Boolean>("blockOverlay.filled")
    private val blockOverlayBordered by ConfigDelegate<Boolean>("blockOverlay.bordered")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Block overlay",
                "Customizes block selection overlay",
                "Visuals",
                ConfigElement(
                    "blockOverlay",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Block fill color",
                ConfigElement(
                    "blockOverlay.overlayColor",
                    ElementType.ColorPicker(Color(0, 255, 255, 38))
                )
            )
            .addFeatureOption(
                "Block border color",
                ConfigElement(
                    "blockOverlay.borderColor",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
            .addFeatureOption(
                "Filled block overlay",
                ConfigElement(
                    "blockOverlay.filled",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Bordered block overlay",
                ConfigElement(
                    "blockOverlay.bordered",
                    ElementType.Switch(true)
                )
            )
    }

    override fun initialize() {
        register<RenderEvent.World.BlockOutline> { event ->
            val blockPos = event.context.blockPos() ?: return@register
            val blockState = event.context.blockState() ?: return@register
            val matrixStack = event.context.matrixStack() ?: return@register
            val consumers = event.context.consumers()
            val camera = client.gameRenderer.mainCamera
            val blockShape = blockState.getShape(
                EmptyBlockGetter.INSTANCE,
                blockPos,
                CollisionContext.of(camera.entity)
            )
            if (blockShape.isEmpty) return@register

            val camPos = camera.position
            event.cancel()

            if (blockOverlayBordered) {
                ShapeRenderer.renderShape(
                    matrixStack,
                    consumers.getBuffer(RenderType.lines()),
                    blockShape,
                    blockPos.x - camPos.x,
                    blockPos.y - camPos.y,
                    blockPos.z - camPos.z,
                    blockBorderColor.rgb
                )
            }

            if (blockOverlayFilled) {
                Render3D.drawFilledShapeVoxel(
                    blockShape.move(blockPos),
                    blockOverlayColor,
                    consumers,
                    matrixStack
                )
            }
        }
    }
}
