package xyz.meowing.zen.config

import xyz.meowing.zen.Zen
import net.minecraft.client.gui.screen.Screen
import xyz.meowing.zen.config.ui.ClickGUI

object ModMenuCompat {
    fun createConfigScreen(parent: Screen?): Screen = Zen.configUI
}

fun ZenConfig(): ClickGUI {
    val clickGUI = ClickGUI()

    Zen.features.forEach { feature ->
        feature.addConfig()
    }

    return clickGUI
}