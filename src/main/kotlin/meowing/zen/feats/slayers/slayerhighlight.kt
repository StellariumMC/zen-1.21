package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.toColorFloat

object slayerhighlight : Feature("slayerhighlight") {
    override fun initialize() {
        register<RenderEvent.EntityPre> { event ->
            if (!slayertimer.isFighting || slayertimer.BossId == -1 || event.entity.id != slayertimer.BossId) return@register
            val entity = event.entity
            val width = entity.width + 0.2
            val height = entity.height
            val entityPos = entity.getLerpedPos(Utils.getPartialTicks())
            val cam = mc.gameRenderer.camera
            val x = entityPos.x - cam.pos.x
            val y = entityPos.y - cam.pos.y
            val z = entityPos.z - cam.pos.z
            val color = Zen.config.slayerhighlightcolor
            RenderUtils.renderEntityOutline(
                event.matrices,
                event.vertex,
                x,
                y,
                z,
                width,
                height,
                color.red.toColorFloat(),
                color.green.toColorFloat(),
                color.blue.toColorFloat(),
                color.alpha.toColorFloat()
            )
        }
    }
}