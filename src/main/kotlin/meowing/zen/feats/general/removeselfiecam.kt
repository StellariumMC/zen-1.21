package meowing.zen.feats.general

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

object removeselfiecam : Feature("removeselfiecam") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Remove selfie camera", ConfigElement(
                "removeselfiecam",
                "Remove selfie camera",
                "Disables the selfie camera.",
                ElementType.Switch(false)
            ))
    }
}