package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import net.minecraft.util.math.RotationAxis
import xyz.meowing.zen.ui.ConfigMenuManager

@Zen.Module
object CustomSpin : Feature("customspin") {
    private val customspinspeed by ConfigDelegate<Double>("customspinspeed")
    private val spineveryone by ConfigDelegate<Boolean>("spineveryone")
    private val spindirection by ConfigDelegate<Int>("spindirection")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Custom spin", "", "Visuals", xyz.meowing.zen.ui.ConfigElement(
                "customspin",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Spin everyone", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "spineveryone",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Custom spin direction", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "spindirection",
                ElementType.Dropdown(listOf("Right", "Left"), 1)
            ))
            .addFeatureOption("Custom spin speed", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "customspinspeed",
                ElementType.Slider(1.0, 20.0, 5.0, true)
            ))

        return configUI
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (spineveryone || event.entity.id == player?.id) {
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getRotation()))
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
        val angle = (fraction * 360f) * customspinspeed.toFloat()
        return if (spindirection == 0) angle - 180f else 180f - angle
    }
}