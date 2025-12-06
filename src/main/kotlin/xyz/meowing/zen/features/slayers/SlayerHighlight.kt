package xyz.meowing.zen.features.slayers

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.skyblock.EntityDetection.getSlayerEntity
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor

@Module
object SlayerHighlight : Feature(
    "slayerHighlight",
    "Slayer highlight",
    "Highlights your slayer boss",
    "Slayers",
    skyblockOnly = true
) {
    private val color by config.colorPicker("Color")

    override fun initialize() {
        register<RenderEvent.Entity.Pre> { event ->
            val entity = event.entity

            if (player?.hasLineOfSight(entity) == true && entity == getSlayerEntity()) {
                entity.glowThisFrame = true
                entity.glowingColor = color.rgb
            }
        }
    }
}