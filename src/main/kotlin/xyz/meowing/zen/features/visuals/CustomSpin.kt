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
object CustomSpin : Feature(
    "customSpin"
) {
    private val customSpinSpeed by ConfigDelegate<Double>("customSpin.customSpinSpeed")
    private val spinEveryone by ConfigDelegate<Boolean>("customSpin.spinEveryone")
    private val spinDirection by ConfigDelegate<Int>("customSpin.spinDirection")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Custom spin",
                "Makes player models spin",
                "Visuals",
                ConfigElement(
                    "customSpin",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Spin everyone",
                ConfigElement(
                    "customSpin.spinEveryone",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Spin direction",
                ConfigElement(
                    "customSpin.spinDirection",
                    ElementType.Dropdown(listOf("Right", "Left"), 1)
                )
            )
            .addFeatureOption(
                "Spin speed",
                ConfigElement(
                    "customSpin.customSpinSpeed",
                    ElementType.Slider(1.0, 20.0, 5.0, true)
                )
            )
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (spinEveryone || event.entity.id == player?.id) {
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
        val angle = (fraction * 360f) * customSpinSpeed.toFloat()
        return if (spinDirection == 0) angle - 180f else 180f - angle
    }
}