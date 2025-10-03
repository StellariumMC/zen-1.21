package xyz.meowing.zen.compat

//#if MC < 1.21.9
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import xyz.meowing.zen.config.ModMenuCompat
import net.minecraft.client.gui.screen.Screen

class ModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen? -> ModMenuCompat.createConfigScreen(parent) }
    }
}
//#endif