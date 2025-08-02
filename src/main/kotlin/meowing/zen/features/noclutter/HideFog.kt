package meowing.zen.features.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature

@Zen.Module
object HideFog : Feature("hidefog") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide fog", ConfigElement(
                "hidefog",
                "Hide fog",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}