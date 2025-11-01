package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.util.math.RotationAxis
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object CustomSpin : Feature("customspin") {
    private val customspinspeed by ConfigDelegate<Double>("customspinspeed")
    private val spineveryone by ConfigDelegate<Boolean>("spineveryone")
    private val spindirection by ConfigDelegate<Int>("spindirection")

    override fun addConfig() {
        ConfigManager
            .addFeature("Custom spin", "", "Visuals", ConfigElement(
                "customspin",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Spin everyone", "", "Options", ConfigElement(
                "spineveryone",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Custom spin direction", "", "Options", ConfigElement(
                "spindirection",
                ElementType.Dropdown(listOf("Right", "Left"), 1)
            ))
            .addFeatureOption("Custom spin speed", "", "Options", ConfigElement(
                "customspinspeed",
                ElementType.Slider(1.0, 20.0, 5.0, true)
            ))
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