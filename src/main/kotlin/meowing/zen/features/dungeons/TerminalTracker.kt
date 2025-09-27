package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.WorldEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object TerminalTracker : Feature("termtracker", area = "catacombs") {
    private var completed: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    private val pattern = Pattern.compile("^(\\w{1,16}) (?:activated|completed) a (\\w+)! \\(\\d/\\d\\)$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Terminal Tracker", ConfigElement(
                "termtracker",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val msg = event.message.string.removeFormatting()
            val matcher = pattern.matcher(msg)

            when {
                msg == "The Core entrance is opening!" -> {
                    completed.forEach { (user, data) ->
                        ChatUtils.addMessage("$prefix §b$user§7 - §b${data["lever"] ?: 0} §flevers §7| §b${data["terminal"] ?: 0} §fterminals §7| §b${data["device"] ?: 0} §fdevices")
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