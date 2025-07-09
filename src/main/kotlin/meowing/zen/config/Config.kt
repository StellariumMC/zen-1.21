package meowing.zen.config

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
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