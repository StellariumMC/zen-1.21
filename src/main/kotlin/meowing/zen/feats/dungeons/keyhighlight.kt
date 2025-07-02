package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.Utils.toColorFloat
import net.minecraft.entity.decoration.ArmorStandEntity

object keyhighlight : Feature("keyhighlight", area = "catacombs") {
    override fun initialize() {
        register<RenderEvent.EntityPre> { event ->
            if (event.entity !is ArmorStandEntity) return@register
            val name = event.entity.name.string.removeFormatting()
            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                val color = Zen.config.keyhighlightcolor
                RenderUtils.renderEntityFilled(
                    event.matrices,
                    event.vertex,
                    entity.x,
                    entity.y + 1.25,
                    entity.z,
                    1f,
                    1f,
                    color.red.toColorFloat(),
                    color.green.toColorFloat(),
                    color.blue.toColorFloat(),
                    color.alpha.toColorFloat()
                )
            }
        }
    }
}