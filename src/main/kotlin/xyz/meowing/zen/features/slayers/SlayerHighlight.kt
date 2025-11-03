package xyz.meowing.zen.features.slayers

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.EntityDetection.getSlayerEntity
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object SlayerHighlight : Feature("slayerhighlight", true) {
    private val slayerhighlightcolor by ConfigDelegate<Color>("slayerhighlightcolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Slayer highlight", "Slayer highlight", "Slayers", ConfigElement(
                "slayerhighlight",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", ConfigElement(
                "slayerhighlightcolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }


    override fun initialize() {
        register<RenderEvent.Entity.Pre> { event ->
            val entity = event.entity

            if (player?.canSee(entity) == true && entity == getSlayerEntity()) {
                entity.glowThisFrame = true
                entity.glowingColor = slayerhighlightcolor.rgb
            }
        }
    }
}