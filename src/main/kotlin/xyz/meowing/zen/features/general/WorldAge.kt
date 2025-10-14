package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.WorldEvent

@Zen.Module
object WorldAge : Feature("worldage") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("World age message", "Send world age", "General", xyz.meowing.zen.ui.ConfigElement(
                "worldage",
                ElementType.Switch(false)
            ))
        return configUI
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