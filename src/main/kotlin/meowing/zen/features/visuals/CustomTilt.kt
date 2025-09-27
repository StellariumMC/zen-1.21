package meowing.zen.features.visuals

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import net.minecraft.util.math.RotationAxis
import kotlin.math.sin

@Zen.Module
object CustomTilt : Feature("customtilt") {
    private val tiltx by ConfigDelegate<Double>("tiltx")
    private val tilty by ConfigDelegate<Double>("tilty")
    private val tiltz by ConfigDelegate<Double>("tiltz")
    private val tilteveryone by ConfigDelegate<Boolean>("tilteveryone")
    private val animatedtilt by ConfigDelegate<Boolean>("animatedtilt")
    private val tiltspeed by ConfigDelegate<Double>("tiltspeed")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Custom tilt", ConfigElement(
                "customtilt",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Custom tilt", "Options", ConfigElement(
                "tiltx",
                "Tilt X",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Custom tilt", "Options", ConfigElement(
                "tilty",
                "Tilt Y",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Custom tilt", "Options", ConfigElement(
                "tiltz",
                "Tilt Z",
                ElementType.Slider(-180.0, 180.0, 0.0, true)
            ))
            .addElement("Visuals", "Custom tilt", "Options", ConfigElement(
                "tilteveryone",
                "Tilt everyone",
                ElementType.Switch(false)
            ))
            .addElement("Visuals", "Custom tilt", "Animate Tilt", ConfigElement(
                "animatedtilt",
                "Animated tilt",
                ElementType.Switch(false)
            ))
            .addElement("Visuals", "Custom tilt", "Animate Tilt", ConfigElement(
                "tiltspeed",
                "Tilt speed",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customtilt"] as? Boolean == true }
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