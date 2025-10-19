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
object HideFallingBlocks : Feature("hidefallingblocks") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Hide falling blocks", "Hide falling blocks", "QoL", xyz.meowing.zen.ui.ConfigElement(
                "hidefallingblocks",
                ElementType.Switch(false)
            ))
        return configUI
    }

}