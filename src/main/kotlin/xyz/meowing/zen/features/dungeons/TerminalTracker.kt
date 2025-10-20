package xyz.meowing.zen.features.dungeons

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object TerminalTracker : Feature("termtracker", area = "catacombs") {
    private var completed: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    private val pattern = Pattern.compile("^(\\w{1,16}) (?:activated|completed) a (\\w+)! \\(\\d/\\d\\)$")

    override fun addConfig() {
        ConfigManager
            .addFeature("Terminal Tracker", "", "Dungeons", ConfigElement(
                "termtracker",
                ElementType.Switch(false)
            ))
    }


    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val msg = event.message.string.removeFormatting()
            val matcher = pattern.matcher(msg)

            when {
                msg == "The Core entrance is opening!" -> {
                    completed.forEach { (user, data) ->
                        KnitChat.fakeMessage("$prefix §b$user§7 - §b${data["lever"] ?: 0} §flevers §7| §b${data["terminal"] ?: 0} §fterminals §7| §b${data["device"] ?: 0} §fdevices")
                    }
                }
                matcher.matches() -> {
                    val user = matcher.group(1)
                    val type = matcher.group(2)
                    if (type in listOf("terminal", "lever", "device"))
                        completed.getOrPut(user) { mutableMapOf() }[type] = (completed[user]?.get(type) ?: 0) + 1
                }
            }
        }

        register<WorldEvent.Change> {
            completed.clear()
        }
    }
}