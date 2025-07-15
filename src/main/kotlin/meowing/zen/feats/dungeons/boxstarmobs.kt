package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

@Zen.Module
object boxstarmobs : Feature("boxstarmobs", area = "catacombs") {
    private val entities = mutableListOf<Int>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Box star mobs", ConfigElement(
                "boxstarmobs",
                "Box star mobs",
                "Highlights star mobs in dungeons.",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Box star mobs", ConfigElement(
                "boxstarmobscolor",
                "Box star mobs color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["boxstarmobs"] as? Boolean == true }
            ))
    }

    override fun initialize() {
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

        register<RenderEvent.EntityGlow> { event ->
            val ent = event.entity
            if (!entities.contains(ent.id)) return@register
            val player = mc.player ?: return@register
            val color = config.boxstarmobscolor
            if (player.canSee(event.entity)) {
                event.shouldGlow = true
                event.glowColor = color.toColorInt()
            }
        }
    }
}