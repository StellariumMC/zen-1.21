package xyz.meowing.zen.features.dungeons

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Module
object TerminalTracker : Feature(
    "terminalTracker",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private var completed: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    private val pattern = Pattern.compile("^(\\w{1,16}) (?:activated|completed) a (\\w+)! \\(\\d/\\d\\)$")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Terminal tracker",
                "Tracks terminal/device/lever completions in dungeons",
                "Dungeons",
                ConfigElement(
                    "terminalTracker",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

            val msg = event.message.string.removeFormatting()
            val matcher = pattern.matcher(msg)

            when {
                msg == "The Core entrance is opening!" -> {
                    completed.toList().forEach { (user, data) ->
                        KnitChat.fakeMessage("$prefix §b$user§7 - §b${data["lever"] ?: 0} §flevers §7| §b${data["terminal"] ?: 0} §fterminals §7| §b${data["device"] ?: 0} §fdevices")
                    }

                    completed.clear()
                }
                matcher.matches() -> {
                    val user = matcher.group(1)
                    val type = matcher.group(2)
                    if (type in listOf("terminal", "lever", "device"))
                        completed.getOrPut(user) { mutableMapOf() }[type] = (completed[user]?.get(type) ?: 0) + 1
                }
            }
        }

        register<LocationEvent.WorldChange> {
            completed.clear()
        }
    }
}