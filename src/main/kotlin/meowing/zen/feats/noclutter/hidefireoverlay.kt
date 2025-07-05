package meowing.zen.feats.noclutter

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

object hidefireoverlay : Feature("hidefireoverlay") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "hidefireoverlay",
                "Hide fire overlay",
                "Cancels the fire overlay rendering on your screen.",
                ElementType.Switch(false)
            ))
    }
}