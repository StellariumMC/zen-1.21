package xyz.meowing.zen.managers.config

import xyz.meowing.zen.config.ui.ConfigData
import xyz.meowing.zen.config.ui.elements.base.ElementType

data class ConfigElement(
    val configKey: String,
    val type: ElementType,
    val shouldShow: (ConfigData) -> Boolean = { true },
    val value: Any? = null,
) {
    var parent: ConfigContainer? = null
}