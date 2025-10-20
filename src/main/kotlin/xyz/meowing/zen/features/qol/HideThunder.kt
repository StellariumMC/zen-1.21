package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Zen.Module
object HideThunder : Feature("hidethunder") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Hide thunder", "Hide thunder", "QoL", ConfigElement(
                "hidethunder",
                ElementType.Switch(false)
            ))
    }
}