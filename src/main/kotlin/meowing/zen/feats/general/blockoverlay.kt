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

object blockoverlay : Feature("blockoverlay") {
    override fun initialize() {
        register<RenderEvent.BlockOutline> { event ->
            val blockPos = event.blockContext.blockPos()
            val consumers = event.worldContext.consumers() ?: return@register
            val camera = mc.gameRenderer.camera
            val blockShape = event.blockContext.blockState().getOutlineShape(
                EmptyBlockView.INSTANCE, blockPos, ShapeContext.of(camera.focusedEntity)
            )
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            RenderSystem.lineWidth(Zen.config.blockoverlaywidth.toFloat())
            event.cancel()
            val color = Zen.config.blockoverlaycolor
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