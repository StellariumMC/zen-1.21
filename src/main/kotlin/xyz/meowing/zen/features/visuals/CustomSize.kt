package xyz.meowing.zen.features.visuals

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.features.Feature

@Module
object CustomSize : Feature(
    "customSize",
    "Custom size",
    "Scales player model size",
    "Visuals"
) {
    private val customX by config.slider("Custom X", 1.0, 0.1, 5.0, true)
    private val customY by config.slider("Custom Y", 1.0, 0.1, 5.0, true)
    private val customZ by config.slider("Custom Z", 1.0, 0.1, 5.0, true)
    private val scaleEveryone by config.switch("Scale everyone", true)

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleEveryone || event.entity.id == player?.id) {
                event.matrices.scale(customX.toFloat(), customY.toFloat(), customZ.toFloat())
            }
        }
    }
}