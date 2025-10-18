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

@Zen.Module
object BlockOverlay : Feature("blockoverlay") {
    private val blockoverlaycolor by ConfigDelegate<Color>("blockoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Block overlay", ConfigElement(
                "blockoverlay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Block overlay", "Options", ConfigElement(
                "blockoverlaycolor",
                "Block overlay color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
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