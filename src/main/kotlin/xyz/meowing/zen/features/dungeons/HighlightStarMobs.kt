package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

@Zen.Module
object HighlightStarMobs : Feature("boxstarmobs", area = "catacombs") {
    private val boxstarmobscolor by ConfigDelegate<Color>("boxstarmobscolor")
    private val entities = mutableListOf<Int>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Highlight star mobs", ConfigElement(
                "boxstarmobs",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Highlight star mobs", "Color", ConfigElement(
                "boxstarmobscolor",
                "Highlight star mobs color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<EntityEvent.Join> { event ->
            if (event.entity !is ArmorStandEntity) return@register
            val ent = event.entity

            /*
             * Modified from Devonian code
             * Under GPL 3.0 License
             */
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

            if (player?.canSee(event.entity) == true) {
                event.shouldGlow = true
                event.glowColor = boxstarmobscolor.toColorInt()
            }
        }
    }
}