package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Module
object RemoveSelfieCam : Feature(
    "removeSelfieCam"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Remove selfie camera",
                "Removes the selfie camera",
                "QoL",
                ConfigElement(
                    "removeSelfieCam",
                    ElementType.Switch(false)
                )
            )
    }
}