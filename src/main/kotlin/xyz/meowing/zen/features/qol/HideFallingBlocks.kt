package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature

@Module
object HideFallingBlocks : Feature(
    "hideFallingBlocks"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Hide falling blocks",
                "Hide falling blocks",
                "QoL",
                ConfigElement(
                    "hideFallingBlocks",
                    ElementType.Switch(false)
                )
            )
    }
}