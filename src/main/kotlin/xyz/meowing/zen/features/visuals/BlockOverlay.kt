package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.world.EmptyBlockView
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.config.ConfigElement
import java.awt.Color
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.config.ConfigManager

@Zen.Module
object BlockOverlay : Feature("blockoverlay") {
    private val blockoverlaycolor by ConfigDelegate<Color>("blockoverlaycolor")
    private val blockbordercolor by ConfigDelegate<Color>("blockbordercolor")
    private val blockoverlayfilled by ConfigDelegate<Boolean>("blockoverlayfilled")
    private val blockoverlaybordered by ConfigDelegate<Boolean>("blockoverlaybordered")

    override fun addConfig() {
        ConfigManager
            .addFeature("Block overlay", "", "Visuals", ConfigElement(
                "blockoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Block Fill Color", "", "Options", ConfigElement(
                "blockoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 38))
            ))
            .addFeatureOption("Block Border Color", "", "Options", ConfigElement(
                "blockbordercolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addFeatureOption("Filled block overlay", "", "Options", ConfigElement(
                "blockoverlayfilled",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Bordered block overlay", "", "Options", ConfigElement(
                "blockoverlaybordered",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.BlockOutline> { event ->
            val blockPos = event.blockPos
            val consumers = event.consumers ?: return@register
            val camera = client.gameRenderer.camera
            val blockShape = event.blockState.getOutlineShape(EmptyBlockView.INSTANCE, blockPos, ShapeContext.of(camera.focusedEntity))
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            event.cancel()

            if (blockoverlaybordered) {
                VertexRendering.drawOutline(
                    event.matrixStack,
                    consumers.getBuffer(RenderLayer.getLines()),
                    blockShape,
                    blockPos.x - camPos.x,
                    blockPos.y - camPos.y,
                    blockPos.z - camPos.z,
                    blockbordercolor.rgb
                )
            }

            if(blockoverlayfilled) {
                Render3D.drawFilledShapeVoxel(
                    blockShape.offset(blockPos),
                    blockoverlaycolor,
                    event.consumers,
                    event.matrixStack
                )
            }
        }
    }
}
