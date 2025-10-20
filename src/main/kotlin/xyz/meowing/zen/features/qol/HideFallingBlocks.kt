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
object HideFallingBlocks : Feature("hidefallingblocks") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Hide falling blocks", "Hide falling blocks", "QoL", ConfigElement(
                "hidefallingblocks",
                ElementType.Switch(false)
            ))
    }
}