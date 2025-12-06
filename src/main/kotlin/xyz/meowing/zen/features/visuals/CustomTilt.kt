package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.features.Feature
import com.mojang.math.Axis
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import kotlin.math.sin

@Module
object CustomTilt : Feature(
    "customTilt",
    "Custom tilt",
    "Customizes player model tilt angles",
    "Visuals"
) {
    private val tiltX by config.slider("Tilt X", 0.0, -180.0, 180.0, true)
    private val tiltY by config.slider("Tilt Y", 0.0, -180.0, 180.0, true)
    private val tiltZ by config.slider("Tilt Z", 0.0, -180.0, 180.0, true)
    private val tiltEveryone by config.switch("Tilt everyone", true)
    private val animatedTilt by config.switch("Animated tilt", false)
    private val tiltSpeed by config.slider("Tilt speed", 1.0, 0.1, 10.0, true)

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (tiltEveryone || event.entity.id == player?.id) {
                val multiplier = if (animatedTilt) sin(System.currentTimeMillis() * tiltSpeed / 1000.0) else 1.0
                event.matrices.mulPose(Axis.XP.rotationDegrees((tiltX * multiplier).toFloat()))
                event.matrices.mulPose(Axis.YP.rotationDegrees((tiltY * multiplier).toFloat()))
                event.matrices.mulPose(Axis.ZP.rotationDegrees((tiltZ * multiplier).toFloat()))
            }
        }
    }
}