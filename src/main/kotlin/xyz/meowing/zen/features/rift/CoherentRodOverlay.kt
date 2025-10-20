package xyz.meowing.zen.features.rift

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.isHolding
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils.toColorInt
import java.awt.Color

@Zen.Module
object CoherentRodOverlay : Feature("coherentrodoverlay", area = "the rift") {
    private val coherentrodoverlaycolor by ConfigDelegate<Color>("coherentrodoverlaycolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Coherent rod", "Coherent rod radius display", "Rift", ConfigElement(
                "coherentrodoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", ConfigElement(
                "coherentrodoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }


    override fun initialize() {
        register<RenderEvent.World> { event ->
            if (isHolding("NEARLY_COHERENT_ROD")) {
                val player = player ?: return@register
                val color = coherentrodoverlaycolor
                Render3D.drawFilledCircle(
                    event.consumers,
                    event.matrixStack,
                    //#if MC >= 1.21.9
                    //$$ player.entityPos,
                    //#else
                    player.pos,
                    //#endif
                    8f,
                    72,
                    color.darker().toColorInt(),
                    color.toColorInt()
                )
            }
        }
    }
}