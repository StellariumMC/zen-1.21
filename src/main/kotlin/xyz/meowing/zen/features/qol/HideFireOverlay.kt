package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature

@Module
object HideFireOverlay : Feature(
    "hideFireOverlay"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Hide fire overlay",
                "Hide fire overlay on screen",
                "QoL",
                ConfigElement(
                    "hideFireOverlay",
                    ElementType.Switch(false)
                )
            )
    }
}