package xyz.meowing.zen.features.visuals

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object CustomSize : Feature(
    "customSize"
) {
    private val customX by ConfigDelegate<Double>("customSize.customX")
    private val customY by ConfigDelegate<Double>("customSize.customY")
    private val customZ by ConfigDelegate<Double>("customSize.customZ")
    private val scaleEveryone by ConfigDelegate<Boolean>("customSize.scaleEveryone")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Custom size",
                "Scales player model size",
                "Visuals",
                ConfigElement(
                    "customSize",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Custom X",
                ConfigElement(
                    "customSize.customX",
                    ElementType.Slider(0.1, 5.0, 1.0, true)
                )
            )
            .addFeatureOption(
                "Custom Y",
                ConfigElement(
                    "customSize.customY",
                    ElementType.Slider(0.1, 5.0, 1.0, true)
                )
            )
            .addFeatureOption(
                "Custom Z",
                ConfigElement(
                    "customSize.customZ",
                    ElementType.Slider(0.1, 5.0, 1.0, true)
                )
            )
            .addFeatureOption(
                "Scale everyone",
                ConfigElement(
                    "customSize.scaleEveryone",
                    ElementType.Switch(true)
                )
            )
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleEveryone || event.entity.id == player?.id) event.matrices.scale(customX.toFloat(), customY.toFloat(), customZ.toFloat())
        }
    }
}