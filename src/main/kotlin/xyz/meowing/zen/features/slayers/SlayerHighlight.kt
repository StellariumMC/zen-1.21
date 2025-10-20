package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection.getSlayerEntity
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.toColorInt
import java.awt.Color

@Zen.Module
object SlayerHighlight : Feature("slayerhighlight", true) {
    private val slayerhighlightcolor by ConfigDelegate<Color>("slayerhighlightcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Slayer highlight", "Slayer highlight", "Slayers", xyz.meowing.zen.ui.ConfigElement(
                "slayerhighlight",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Color", "", xyz.meowing.zen.ui.ConfigElement(
                "slayerhighlightcolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
        return configUI
    }


    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            if (player?.canSee(event.entity) == true && event.entity == getSlayerEntity()) {
                event.shouldGlow = true
                event.glowColor = slayerhighlightcolor.toColorInt()
            }
        }
    }
}