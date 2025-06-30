package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.RenderWorldPostEntitiesEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.Utils.toColorFloat

object slayerhighlight : Feature("slayerhighlight") {
    override fun initialize() {
        register<RenderWorldPostEntitiesEvent> { event ->
            if (!slayertimer.isFighting || slayertimer.BossId == -1) return@register
            val bossEntity = mc.world!!.getEntityById(slayertimer.BossId) ?: return@register
            val width = (bossEntity.width + 0.5).toFloat()
            val height = (bossEntity.height + 0.25).toFloat()
            val entityPos = bossEntity.getLerpedPos(event.context!!.tickCounter().getTickProgress(true))
            val x = entityPos.x - mc.gameRenderer.camera.pos.x
            val y = entityPos.y - mc.gameRenderer.camera.pos.y
            val z = entityPos.z - mc.gameRenderer.camera.pos.z
            val color = Zen.config.slayerhighlightcolor
            RenderUtils.renderEntityBox(
                event.context.matrixStack(),
                event.context.consumers(),
                x, y, z, width, height,
                color.red.toColorFloat(), color.green.toColorFloat(), color.blue.toColorFloat(), color.alpha.toColorFloat()
            )
        }
    }
}