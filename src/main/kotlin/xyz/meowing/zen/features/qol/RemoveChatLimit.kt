package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature

@Module
object RemoveChatLimit : Feature(
    "removeChatLimit"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Remove chat history limit",
                "Removes the chat history limit",
                "QoL",
                ConfigElement(
                    "removeChatLimit",
                    ElementType.Switch(false)
                )
            )
    }
}