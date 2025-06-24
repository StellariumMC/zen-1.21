package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.RenderWorldPostEntitiesEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils

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
            RenderUtils.renderEntityBox(
                event.context.matrixStack(),
                event.context.consumers(),
                x, y, z, width, height,
                0f, 1f, 1f, 0.5f
            )
        }
    }
}