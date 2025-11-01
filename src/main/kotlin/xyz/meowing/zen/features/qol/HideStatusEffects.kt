package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object HideStatusEffects : Feature("hidestatuseffects") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Hide status effects", "Hide status effects", "QoL", ConfigElement(
                "hidestatuseffects",
                ElementType.Switch(false)
            ))
    }
}