package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object RemoveChatLimit : Feature("removechatlimit") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Remove chat history limit", "", "QoL", ConfigElement(
                "removechatlimit",
                ElementType.Switch(false)
            ))
    }
}