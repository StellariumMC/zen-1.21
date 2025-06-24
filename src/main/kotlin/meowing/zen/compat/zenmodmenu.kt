package meowing.zen.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import meowing.zen.config.ZenConfig
import net.minecraft.client.gui.screen.Screen

class zenmodmenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen? -> ZenConfig.createConfigScreen(parent) }
    }
}