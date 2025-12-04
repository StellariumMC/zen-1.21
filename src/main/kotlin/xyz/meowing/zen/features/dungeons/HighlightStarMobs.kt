package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.world.entity.decoration.ArmorStand
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object HighlightStarMobs : Feature(
    "highlightStarMobs",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private val entities = mutableListOf<Int>()
    private val starMobsColor by ConfigDelegate<Color>("highlightStarMobs.color")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Highlight star mobs",
                "Highlights starred mobs in dungeons",
                "Dungeons",
                ConfigElement(
                    "highlightStarMobs",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Highlight star mobs color",
                ConfigElement(
                    "highlightStarMobs.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }

    override fun initialize() {
        register<EntityEvent.Join> { event ->
            if (event.entity !is ArmorStand) return@register
            val ent = event.entity

            /*
             * Modified from Devonian code
             * Under GPL 3.0 License
             */
            TickScheduler.Server.schedule(2) {
                val name = ent.name.string
                if (!name.contains("✯ ")) return@schedule
                val id = ent.id
                val offset = if (name.contains("Withermancer")) 3 else 1
                entities.add(id - offset)
            }
        }

        register<LocationEvent.WorldChange> {
            entities.clear()
        }

        register<RenderEvent.Entity.Pre> { event ->
            val entity = event.entity
            if (!entities.contains(entity.id)) return@register

            if (player?.hasLineOfSight(entity) == true) {
                entity.glowThisFrame = true
                entity.glowingColor = starMobsColor.rgb
            }
        }
    }
}