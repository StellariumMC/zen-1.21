package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object RemoveSelfieCam : Feature("removeselfiecam") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Remove selfie camera", "", "QoL", xyz.meowing.zen.ui.ConfigElement(
                "removeselfiecam",
                ElementType.Switch(false)
            ))
        return configUI
    }

}