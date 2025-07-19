package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

@Zen.Module
object nofog : Feature("nofog") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "nofog",
                "Hide fog",
                "Hides fog during rendering.",
                ElementType.Switch(false)
            ))
    }
}