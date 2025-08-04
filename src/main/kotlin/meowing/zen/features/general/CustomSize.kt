package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature

@Zen.Module
object CustomSize : Feature("customsize") {
    private val customX by ConfigDelegate<Double>("customX")
    private val customY by ConfigDelegate<Double>("customY")
    private val customZ by ConfigDelegate<Double>("customZ")
    private val scaleeveryone by ConfigDelegate<Boolean>("scaleeveryone")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Custom size", ConfigElement(
                "customsize",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Custom size", "Size", ConfigElement(
                "customX",
                "Custom X",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("General", "Custom size", "Size", ConfigElement(
                "customY",
                "Custom Y",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("General", "Custom size", "Size", ConfigElement(
                "customZ",
                "Custom Z",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("General", "Custom size", "Other Options", ConfigElement(
                "scaleeveryone",
                "Scale everyone",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.PlayerPre> { event ->
            if (scaleeveryone || event.entity.id == player?.id) event.matrices.scale(customX.toFloat(), customY.toFloat(), customZ.toFloat())
        }
    }
}