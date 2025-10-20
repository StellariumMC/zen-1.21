package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object RemoveSelfieCam : Feature("removeselfiecam") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Remove selfie camera", "", "QoL", ConfigElement(
                "removeselfiecam",
                ElementType.Switch(false)
            ))
    }
}