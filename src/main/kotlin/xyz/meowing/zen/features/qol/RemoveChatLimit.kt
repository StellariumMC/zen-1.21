package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object RemoveChatLimit : Feature("removechatlimit") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Remove chat history limit", "", "QoL", xyz.meowing.zen.ui.ConfigElement(
                "removechatlimit",
                ElementType.Switch(false)
            ))
        return configUI
    }

}