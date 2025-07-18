package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Zen.Module
object hidefallingblocks : Feature("hidefallingblocks") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "hidefallingblocks",
                "Hide falling blocks",
                "Cancels the animation of the blocks falling",
                ElementType.Switch(false)
            ))
    }
}