package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.world.EmptyBlockView
import meowing.zen.events.RenderEvent
import java.awt.Color

@Zen.Module
object BlockOverlay : Feature("blockoverlay") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlay",
                "Block overlay",
                "Custom block highlighting",
                ElementType.Switch(false)
            ))
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlaycolor",
                "Block overlay color",
                "The color for Block overlay",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["blockoverlay"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.BlockOutline> { event ->
            val blockPos = event.blockContext.blockPos()
            val consumers = event.worldContext.consumers() ?: return@register
            val camera = mc.gameRenderer.camera
            val blockShape = event.blockContext.blockState().getOutlineShape(EmptyBlockView.INSTANCE, blockPos, ShapeContext.of(camera.focusedEntity))
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            event.cancel()
            VertexRendering.drawOutline(
                event.worldContext.matrixStack(),
                consumers.getBuffer(RenderLayer.getLines()),
                blockShape,
                blockPos.x - camPos.x,
                blockPos.y - camPos.y,
                blockPos.z - camPos.z,
                config.blockoverlaycolor.toColorInt()
            )
        }
    }
}