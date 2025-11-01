package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.decoration.ArmorStandEntity
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

@Module
object HighlightStarMobs : Feature("boxstarmobs", island = SkyBlockIsland.THE_CATACOMBS) {
    private val boxstarmobscolor by ConfigDelegate<Color>("boxstarmobscolor")
    private val entities = mutableListOf<Int>()

    override fun addConfig() {
        ConfigManager
            .addFeature("Highlight star mobs", "", "Dungeons", ConfigElement(
                "boxstarmobs",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Highlight star mobs color", "Highlight star mobs color", "Color", ConfigElement(
                "boxstarmobscolor",
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

        register<LocationEvent.WorldChange> {
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