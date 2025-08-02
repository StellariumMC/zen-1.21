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
object HideFallingBlocks : Feature("hidefallingblocks") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide falling blocks", ConfigElement(
                "hidefallingblocks",
                "Hide falling blocks",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}