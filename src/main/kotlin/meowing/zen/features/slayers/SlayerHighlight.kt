package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.api.EntityDetection.getSlayerEntity
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Utils.toColorInt
import java.awt.Color

@Zen.Module
object SlayerHighlight : Feature("slayerhighlight") {
    private val slayerhighlightcolor by ConfigDelegate<Color>("slayerhighlightcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer highlight", ConfigElement(
                "slayerhighlight",
                "Slayer highlight",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Slayer highlight", "Color", ConfigElement(
                "slayerhighlightcolor",
                "Slayer highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
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