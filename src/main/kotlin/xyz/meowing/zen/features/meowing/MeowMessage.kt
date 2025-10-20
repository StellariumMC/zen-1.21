package xyz.meowing.zen.features.meowing

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.features.Feature
import kotlin.random.Random

@Zen.Module
object MeowMessage : Feature("meowmessage") {
    private val variants = listOf("meow", "mew", "mrow", "nyaa", "purr", "mrrp", "meoww", "nya")

    override fun addConfig() {
        ConfigManager
            .addFeature("Cat Speak", "Cat Speak", "Meowing", ConfigElement(
                "meowmessage",
                ElementType.Switch(false)
            ))
    }


    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (event.chatUtils) return@register
            event.cancel()

            if (event.message.startsWith("/")) {
                val parts = event.message.split(" ")
                if (parts.size > 1) {
                    val message = "${parts[0]} ${transform(parts.drop(1).joinToString(" "))}"
                    EventBus.messages.add(message)
                    KnitChat.sendCommand(message)
                } else {
                    EventBus.messages.add(event.message)
                    KnitChat.sendCommand(event.message)
                }
            } else {
                val message = transform(event.message)
                EventBus.messages.add(message)
                KnitChat.sendMessage(message)
            }
        }
    }

    private fun transform(message: String): String {
        val words = message.split(" ")
        val result = mutableListOf<String>()
        for (word in words) {
            result.add(word)
            if (!word.startsWith("/") && Random.nextBoolean()) {
                result.add(variants.random())
                if (Random.nextFloat() < 0.25f) result.add(variants.random())
            }
        }
        return result.joinToString(" ")
    }
}