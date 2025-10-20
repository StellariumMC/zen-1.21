package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.ui.ConfigManager

@Zen.Module
object CustomSize : Feature("customsize") {
    private val customX by ConfigDelegate<Double>("customX")
    private val customY by ConfigDelegate<Double>("customY")
    private val customZ by ConfigDelegate<Double>("customZ")
    private val scaleeveryone by ConfigDelegate<Boolean>("scaleeveryone")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigManager
            .addFeature("Custom size", "", "Visuals", xyz.meowing.zen.ui.ConfigElement(
                "customsize",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Custom X", "", "Size", xyz.meowing.zen.ui.ConfigElement(
                "customX",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addFeatureOption("Custom Y", "", "Size", xyz.meowing.zen.ui.ConfigElement(
                "customY",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addFeatureOption("Custom Z", "", "Size", xyz.meowing.zen.ui.ConfigElement(
                "customZ",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addFeatureOption("Scale everyone", "", "Other Options", xyz.meowing.zen.ui.ConfigElement(
                "scaleeveryone",
                ElementType.Switch(true)
            ))

        return configUI
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleeveryone || event.entity.id == player?.id) event.matrices.scale(customX.toFloat(), customY.toFloat(), customZ.toFloat())
        }
    }
}