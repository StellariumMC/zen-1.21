package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.util.math.RotationAxis
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import kotlin.math.sin
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object CustomTilt : Feature("customtilt") {
    private val tiltx by ConfigDelegate<Double>("tiltx")
    private val tilty by ConfigDelegate<Double>("tilty")
    private val tiltz by ConfigDelegate<Double>("tiltz")
    private val tilteveryone by ConfigDelegate<Boolean>("tilteveryone")
    private val animatedtilt by ConfigDelegate<Boolean>("animatedtilt")
    private val tiltspeed by ConfigDelegate<Double>("tiltspeed")

    override fun addConfig() {
        ConfigManager
            .addFeature("Custom tilt", "", "Visuals", ConfigElement(
                "customtilt",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Tilt X", "", "Options", ConfigElement(
                "tiltx",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addFeatureOption("Tilt Y", "", "Options", ConfigElement(
                "tilty",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addFeatureOption("Tilt Z", "", "Options", ConfigElement(
                "tiltz",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addFeatureOption("Tilt everyone", "", "Options", ConfigElement(
                "tilteveryone",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Animated tilt", "", "Options", ConfigElement(
                "animatedtilt",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Tilt speed", "", "Options", ConfigElement(
                "tiltspeed",
                ElementType.Slider(0.1, 10.0, 1.0, true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (tilteveryone || event.entity.id == player?.id) {
                val multiplier = if (animatedtilt) sin(System.currentTimeMillis() * tiltspeed / 1000.0) else 1.0
                event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((tiltx * multiplier).toFloat()))
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((tilty * multiplier).toFloat()))
                event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((tiltz * multiplier).toFloat()))
            }
        }
    }
}