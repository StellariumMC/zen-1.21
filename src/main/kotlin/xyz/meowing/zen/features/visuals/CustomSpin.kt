package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.features.Feature
import com.mojang.math.Axis
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent

@Module
object CustomSpin : Feature(
    "customSpin",
    "Custom spin",
    "Makes player models spin",
    "Visuals"
) {
    private val customSpinSpeed by config.slider("Spin speed", 5.0, 1.0, 20.0, true)
    private val spinEveryone by config.switch("Spin everyone")
    private val spinDirection by config.dropdown("Spin direction", listOf("Right", "Left"), 1)

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (spinEveryone || event.entity.id == player?.id) {
                event.matrices.mulPose(Axis.YP.rotationDegrees(getRotation()))
            }
        }
    }

    /*
     * Modified from NoammAddons code
     * Under GPL 3.0 License
     */
    private fun getRotation(): Float {
        val millis = System.currentTimeMillis() % 4000
        val fraction = millis / 4000f
        val angle = (fraction * 360f) * customSpinSpeed.toFloat()
        return if (spinDirection == 0) angle - 180f else 180f - angle
    }
}