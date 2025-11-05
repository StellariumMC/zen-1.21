package xyz.meowing.zen.features.general

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import java.util.regex.Pattern

@Module
object GuildJoinLeave : Feature(
    "guildJoinLeave"
) {
    private val guildPattern = Pattern.compile("^§2Guild > §r(§[a-f0-9])(\\w+) §r§e(\\w+)\\.§r$")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Clean guild join/leave",
                "Reformats guild join/leave messages",
                "General",
                ConfigElement(
                    "guildJoinLeave",
                    ElementType.Switch(false)
                )
            )
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
                KnitChat.fakeMessage(message)
            }
        }
    }
}

@Module
object FriendJoinLeave : Feature(
    "friendJoinLeave"
) {
    private val friendPattern = Pattern.compile("^§aFriend > §r(§[a-f0-9])(\\w+) §r§e(\\w+)\\.§r$")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Clean friend join/leave",
                "Reformats friend join/leave messages",
                "General",
                ConfigElement(
                    "friendJoinLeave",
                    ElementType.Switch(false)
                )
            )
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
                KnitChat.fakeMessage(message)
            }
        }
    }
}