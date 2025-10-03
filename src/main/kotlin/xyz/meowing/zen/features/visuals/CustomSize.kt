package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature

@Zen.Module
object CustomSize : Feature("customsize") {
    private val customX by ConfigDelegate<Double>("customX")
    private val customY by ConfigDelegate<Double>("customY")
    private val customZ by ConfigDelegate<Double>("customZ")
    private val scaleeveryone by ConfigDelegate<Boolean>("scaleeveryone")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Custom size", ConfigElement(
                "customsize",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Custom size", "Size", ConfigElement(
                "customX",
                "Custom X",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("Visuals", "Custom size", "Size", ConfigElement(
                "customY",
                "Custom Y",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("Visuals", "Custom size", "Size", ConfigElement(
                "customZ",
                "Custom Z",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("Visuals", "Custom size", "Other Options", ConfigElement(
                "scaleeveryone",
                "Scale everyone",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleeveryone || event.entity.id == player?.id) event.matrices.scale(customX.toFloat(), customY.toFloat(), customZ.toFloat())
        }
    }
}