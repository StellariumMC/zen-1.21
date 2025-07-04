package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.world.EmptyBlockView
import com.mojang.blaze3d.systems.RenderSystem
import meowing.zen.events.RenderEvent
import java.awt.Color

object blockoverlay : Feature("blockoverlay") {
    private var color = Color(0, 255, 255, 127)
    private var width = 2f

    override fun initialize() {
        Zen.registerCallback("blockoverlaycolor") { newval ->
            color = newval as Color
        }
        Zen.registerCallback("blockoverlaywidth") { newval ->
            width = (newval as Double).toFloat()
        }

        register<RenderEvent.BlockOutline> { event ->
            val blockPos = event.blockContext.blockPos()
            val consumers = event.worldContext.consumers() ?: return@register
            val camera = mc.gameRenderer.camera
            val blockShape = event.blockContext.blockState().getOutlineShape(EmptyBlockView.INSTANCE, blockPos, ShapeContext.of(camera.focusedEntity))
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            RenderSystem.lineWidth(width)
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