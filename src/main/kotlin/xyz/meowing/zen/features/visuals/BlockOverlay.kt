package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.world.EmptyBlockView
import java.awt.Color
import xyz.meowing.zen.ui.ConfigMenuManager

@Zen.Module
object BlockOverlay : Feature("blockoverlay") {
    private val blockoverlaycolor by ConfigDelegate<Color>("blockoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Block overlay", "", "Visuals", xyz.meowing.zen.ui.ConfigElement(
                "blockoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Block overlay color", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "blockoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))

        return configUI
    }

    override fun initialize() {
        register<RenderEvent.BlockOutline> { event ->
            val blockPos = event.blockPos
            val consumers = event.consumers ?: return@register
            val camera = mc.gameRenderer.camera
            val blockShape = event.blockState.getOutlineShape(EmptyBlockView.INSTANCE, blockPos, ShapeContext.of(camera.focusedEntity))
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            event.cancel()
            VertexRendering.drawOutline(
                event.matrixStack,
                consumers.getBuffer(RenderLayer.getLines()),
                blockShape,
                blockPos.x - camPos.x,
                blockPos.y - camPos.y,
                blockPos.z - camPos.z,
                blockoverlaycolor.rgb
            )
        }
    }
}