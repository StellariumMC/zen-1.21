package xyz.meowing.zen.config

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import net.minecraft.client.gui.screen.Screen

object ModMenuCompat {
    fun createConfigScreen(parent: Screen?): Screen = Zen.configUI
}

fun ZenConfig(): ConfigUI {
    var configUI = ConfigUI("config")
    Zen.features.forEach { feature ->
        configUI = feature.addConfig(configUI)
    }
    return configUI
}