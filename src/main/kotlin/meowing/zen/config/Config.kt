package meowing.zen.config

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import net.minecraft.client.gui.screen.Screen

object ModMenuCompat {
    fun createConfigScreen(parent: Screen?): Screen = Zen.configUI
}

fun ZenConfig(): ConfigUI {
    var configUI = ConfigUI("ZenConfig")
    println("Creating ZenConfig with ${Zen.features.size} features")
    Zen.features.forEach { feature ->
        println("Adding config for feature: ${feature.javaClass.simpleName}")
        configUI = feature.addConfig(configUI)
    }
    return configUI
}