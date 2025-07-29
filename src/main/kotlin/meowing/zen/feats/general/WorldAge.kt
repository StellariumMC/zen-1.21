package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.events.EventBus
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.utils.TickUtils

@Zen.Module
object WorldAge : Feature("worldage") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "World age message", ConfigElement(
                "worldage",
                "Send world age",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<WorldEvent.Change> {
            val currentWorld = world ?: return@register

            createTimer(20,
                onComplete = {
                    val daysRaw = currentWorld.time / 24000.0
                    val days = if (daysRaw % 1.0 == 0.0) daysRaw.toInt().toString() else "%.1f".format(daysRaw)
                    ChatUtils.addMessage("$prefix §fWorld is §b$days §fdays old.")
                }
            )
        }
    }
}