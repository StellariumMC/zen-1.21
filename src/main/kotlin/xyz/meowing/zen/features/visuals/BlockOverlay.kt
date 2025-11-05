package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.world.EmptyBlockView
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
            val camera = client.gameRenderer.camera
            val blockShape = blockState.getOutlineShape(
                EmptyBlockView.INSTANCE,
                blockPos,
                ShapeContext.of(camera.focusedEntity)
            )
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            event.cancel()

            if (blockOverlayBordered) {
                VertexRendering.drawOutline(
                    matrixStack,
                    consumers.getBuffer(RenderLayer.getLines()),
                    blockShape,
                    blockPos.x - camPos.x,
                    blockPos.y - camPos.y,
                    blockPos.z - camPos.z,
                    blockBorderColor.rgb
                )
            }

            if (blockOverlayFilled) {
                Render3D.drawFilledShapeVoxel(
                    blockShape.offset(blockPos),
                    blockOverlayColor,
                    consumers,
                    matrixStack
                )
            }
        }
    }
}
