package xyz.meowing.zen.features.qol

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object SameServerAlert : Feature("serveralert") {
    private val regex = "Sending to server (.+)\\.\\.\\.".toRegex()
    private val servers = mutableMapOf<String, Long>()

    override fun addConfig() {
        ConfigManager
            .addFeature("Same server alert", "", "QoL", ConfigElement(
                "serveralert",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            regex.find(event.message.string.removeFormatting())?.let { match ->
                val server = match.groupValues[1]
                val currentTime = TimeUtils.now.toMillis

                servers[server]?.let { lastJoined ->
                    KnitChat.fakeMessage("$prefix §fLast joined §b$server §f- §b${(currentTime - lastJoined) / 1000}s §fago")
                }

                servers[server] = currentTime
            }
        }
    }
}