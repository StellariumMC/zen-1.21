package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import java.util.regex.Pattern

@Zen.Module
object guildjoinleave : Feature("guildjoinleave") {
    private val guildPattern = Pattern.compile("^§2Guild > §r(§[a-f0-9])(\\w+) §r§e(\\w+)\\.§r$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Clean Chat", "Clean messages", ConfigElement(
                "guildjoinleave",
                "Clean guild join/leave",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val matcher = guildPattern.matcher(event.message.string)
            if (matcher.matches()) {
                event.cancel()
                val color = matcher.group(1) ?: ""
                val user = matcher.group(2) ?: ""
                val action = matcher.group(3) ?: ""
                val message = when (action) {
                    "joined" -> "§8G §a>> $color$user"
                    "left" -> "§8G §c<< $color$user"
                    else -> return@register
                }
                ChatUtils.addMessage(message)
            }
        }
    }
}

@Zen.Module
object friendjoinleave : Feature("friendjoinleave") {
    private val friendPattern = Pattern.compile("^§aFriend > §r(§[a-f0-9])(\\w+) §r§e(\\w+)\\.§r$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Clean Chat", "Clean messages", ConfigElement(
                "friendjoinleave",
                "Clean friend join/leave",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val matcher = friendPattern.matcher(event.message.string)
            if (matcher.matches()) {
                event.cancel()
                val color = matcher.group(1) ?: ""
                val user = matcher.group(2) ?: ""
                val action = matcher.group(3) ?: ""
                val message = when (action) {
                    "joined" -> "§8F §a>> $color$user"
                    "left" -> "§8F §c<< $color$user"
                    else -> return@register
                }
                ChatUtils.addMessage(message)
            }
        }
    }
}