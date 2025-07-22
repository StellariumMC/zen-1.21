package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt

@Zen.Module
object SlayerHighlight : Feature("slayerhighlight", true) {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlight",
                "Slayer highlight",
                "Highlights your slayer boss.",
                ElementType.Switch(false),
                { config -> config["slayertimer"] as? Boolean == true}
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            if (!SlayerTimer.isFighting || SlayerTimer.BossId == -1 || event.entity.id != SlayerTimer.BossId) return@register
            if (mc.player?.canSee(event.entity) == true) {
                event.shouldGlow = true
                event.glowColor = config.slayerhighlightcolor.toColorInt()
            }
        }
    }
}