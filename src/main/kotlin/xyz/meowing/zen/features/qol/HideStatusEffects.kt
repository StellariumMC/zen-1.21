package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Zen.Module
object HideStatusEffects : Feature("hidestatuseffects") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Hide status effects", "Hide status effects", "QoL", xyz.meowing.zen.ui.ConfigElement(
                "hidestatuseffects",
                ElementType.Switch(false)
            ))
        return configUI
    }
}