package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import java.awt.Color

@Zen.Module
object SlayerHighlight : Feature("slayerhighlight") {
    private val slayerhighlightcolor by ConfigDelegate<Color>("slayerhighlightcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlight",
                "Slayer highlight",
                "Highlights your slayer boss.",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlightcolor",
                "Slayer highlight color",
                "Slayer highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            if (!SlayerTimer.isFighting || SlayerTimer.BossId == -1 || event.entity.id != SlayerTimer.BossId) return@register
            if (player?.canSee(event.entity) == true) {
                event.shouldGlow = true
                event.glowColor = slayerhighlightcolor.toColorInt()
            }
        }
    }
}