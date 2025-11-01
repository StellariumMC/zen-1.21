package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Module
object HideThunder : Feature("hidethunder") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Hide thunder", "Hide thunder", "QoL", ConfigElement(
                "hidethunder",
                ElementType.Switch(false)
            ))
    }
}