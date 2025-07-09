package meowing.zen.feats.general

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

object customsize : Feature("customsize") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Custom model", ConfigElement(
                "customsize",
                "Custom player model size",
                "Changes the size of your player model",
                ElementType.Switch(false)
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customX",
                "Custom X",
                "X scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customY",
                "Custom Y",
                "Y scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customZ",
                "Custom Z",
                "Z scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customself",
                "Only scale yourself",
                "Enable to only scale your player model, disable to scale all players.",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.PlayerPre> { event ->
            if (!config.customself || event.entity.id == mc.player?.id) event.matrices.scale(config.customX, config.customY, config.customZ)
        }
    }
}