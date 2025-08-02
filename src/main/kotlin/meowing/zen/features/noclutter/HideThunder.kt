package meowing.zen.features.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Zen.Module
object HideThunder : Feature("hidethunder") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide thunder", ConfigElement(
                "hidethunder",
                "Hide thunder",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}