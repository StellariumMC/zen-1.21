package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt

object slayerhighlight : Feature("slayerhighlight") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlight",
                "Slayer highlight",
                "Highlights your slayer boss.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            if (!slayertimer.isFighting || slayertimer.BossId == -1 || event.entity.id != slayertimer.BossId) return@register
            val player = mc.player ?: return@register
            if (player.canSee(event.entity)) {
                event.shouldGlow = true
                event.glowColor = config.slayerhighlightcolor.toColorInt()
            }
        }
    }
}