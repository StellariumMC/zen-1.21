package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import com.mojang.math.Axis
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import kotlin.math.sin
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object CustomTilt : Feature(
    "customTilt"
) {
    private val tiltX by ConfigDelegate<Double>("customTilt.tiltX")
    private val tiltY by ConfigDelegate<Double>("customTilt.tiltY")
    private val tiltZ by ConfigDelegate<Double>("customTilt.tiltZ")
    private val tiltEveryone by ConfigDelegate<Boolean>("customTilt.tiltEveryone")
    private val animatedTilt by ConfigDelegate<Boolean>("customTilt.animatedTilt")
    private val tiltSpeed by ConfigDelegate<Double>("customTilt.tiltSpeed")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Custom tilt",
                "Customizes player model tilt angles",
                "Visuals",
                ConfigElement(
                    "customTilt",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Tilt X",
                ConfigElement(
                    "customTilt.tiltX",
                    ElementType.Slider(-180.0, 180.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Tilt Y",
                ConfigElement(
                    "customTilt.tiltY",
                    ElementType.Slider(-180.0, 180.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Tilt Z",
                ConfigElement(
                    "customTilt.tiltZ",
                    ElementType.Slider(-180.0, 180.0, 0.0, true)
                )
            )
            .addFeatureOption(
                "Tilt everyone",
                ConfigElement(
                    "customTilt.tiltEveryone",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Animated tilt",
                ConfigElement(
                    "customTilt.animatedTilt",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Tilt speed",
                ConfigElement(
                    "customTilt.tiltSpeed",
                    ElementType.Slider(0.1, 10.0, 1.0, true)
                )
            )
    }

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