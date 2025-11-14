package xyz.meowing.zen.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import xyz.meowing.zen.managers.config.ConfigManager

class ModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { _: Screen? -> ConfigManager.configUI }
    }
}