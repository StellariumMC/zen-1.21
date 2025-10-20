package xyz.meowing.zen.features.visuals

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigManager

@Zen.Module
object CustomSize : Feature("customsize") {
    private val customX by ConfigDelegate<Double>("customX")
    private val customY by ConfigDelegate<Double>("customY")
    private val customZ by ConfigDelegate<Double>("customZ")
    private val scaleeveryone by ConfigDelegate<Boolean>("scaleeveryone")

    override fun addConfig() {
        ConfigManager
            .addFeature("Custom size", "", "Visuals", ConfigElement(
                "customsize",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Custom X", "", "Size", ConfigElement(
                "customX",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addFeatureOption("Custom Y", "", "Size", ConfigElement(
                "customY",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addFeatureOption("Custom Z", "", "Size", ConfigElement(
                "customZ",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addFeatureOption("Scale everyone", "", "Other Options", ConfigElement(
                "scaleeveryone",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleeveryone || event.entity.id == player?.id) event.matrices.scale(customX.toFloat(), customY.toFloat(), customZ.toFloat())
        }
    }
}