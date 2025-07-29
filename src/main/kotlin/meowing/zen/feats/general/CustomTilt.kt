package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
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
            .addElement("General", "Custom tilt", ConfigElement(
                "customtilt",
                "Custom tilt",
                "Tilts your player model on various axes",
                ElementType.Switch(false)
            ))
            .addElement("General", "Custom tilt", ConfigElement(
                "tiltx",
                "Tilt X",
                "X axis tilt angle",
                ElementType.Slider(-180.0, 180.0, 0.0, true),
                { config -> config["customtilt"] as? Boolean == true }
            ))
            .addElement("General", "Custom tilt", ConfigElement(
                "tilty",
                "Tilt Y",
                "Y axis tilt angle",
                ElementType.Slider(-180.0, 180.0, 0.0, true),
                { config -> config["customtilt"] as? Boolean == true }
            ))
            .addElement("General", "Custom tilt", ConfigElement(
                "tiltz",
                "Tilt Z",
                "Z axis tilt angle",
                ElementType.Slider(-180.0, 180.0, 0.0, true),
                { config -> config["customtilt"] as? Boolean == true }
            ))
            .addElement("General", "Custom tilt", ConfigElement(
                "animatedtilt",
                "Animated tilt",
                "Smoothly animate the tilt values",
                ElementType.Switch(false),
                { config -> config["customtilt"] as? Boolean == true }
            ))
            .addElement("General", "Custom tilt", ConfigElement(
                "tiltspeed",
                "Tilt speed",
                "Speed of tilt animation",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customtilt"] as? Boolean == true }
            ))
            .addElement("General", "Custom tilt", ConfigElement(
                "tilteveryone",
                "Tilt everyone",
                "Apply tilt to all players",
                ElementType.Switch(false),
                { config -> config["customtilt"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.PlayerPre> { event ->
            if (tilteveryone || event.entity == player) {
                val multiplier = if (animatedtilt) sin(System.currentTimeMillis() * tiltspeed / 1000.0) else 1.0
                event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((tiltx * multiplier).toFloat()))
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((tilty * multiplier).toFloat()))
                event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((tiltz * multiplier).toFloat()))
            }
        }
    }
}