package meowing.zen.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import meowing.zen.config.ModMenuCompat
import net.minecraft.client.gui.screen.Screen

class ModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen? -> ModMenuCompat.createConfigScreen(parent) }
    }
}

// Testing, ignore