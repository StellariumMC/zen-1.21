package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object HideStatusEffects : Feature(
    "hideStatusEffects"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Hide status effects",
                "Hides status effects in your inventory",
                "QoL",
                ConfigElement(
                    "hideStatusEffects",
                    ElementType.Switch(false)
                )
            )
    }
}