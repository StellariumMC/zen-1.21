package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.toColorFloat
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

object boxstarmobs : Feature("boxstarmobs", area = "catacombs") {
    private val entities = mutableListOf<Int>()

    override fun initialize() {
        var color = Color(0, 255, 255, 127)

        Zen.registerCallback("boxstarmobscolor") { newval ->
            color = newval as Color
        }

        register<EntityEvent.Join> { event ->
            if (event.entity !is ArmorStandEntity) return@register
            val ent = event.entity

            TickUtils.scheduleServer(2) {
                val name = ent.name.string
                if (!name.contains("✯ ")) return@scheduleServer
                val id = ent.id
                val offset = if (name.contains("Withermancer")) 3 else 1
                entities.add(id - offset)
            }
        }

        register<WorldEvent.Change> {
            entities.clear()
        }

        register<RenderEvent.EntityPre> { event ->
            val ent = event.entity
            if (!entities.contains(ent.id)) return@register
            val matrices = event.matrices
            val vertex = event.vertex

            val cam = mc.gameRenderer.camera
            val width = ent.width + 0.2
            val height = ent.height
            val entityPos = ent.getLerpedPos(Utils.getPartialTicks())
            val x = entityPos.x - cam.pos.x
            val y = entityPos.y - cam.pos.y
            val z = entityPos.z - cam.pos.z
            RenderUtils.renderEntityOutline(
                matrices,
                vertex,
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