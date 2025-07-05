package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.world.EmptyBlockView
import meowing.zen.events.RenderEvent
import java.awt.Color

object blockoverlay : Feature("blockoverlay") {
    override fun initialize() {
        var color = Color(255, 255, 255, 255)

        Zen.registerCallback("blockoverlaycolor") { newval ->
            color = newval as Color
        }

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
                color.toColorInt()
            )
        }
    }
}