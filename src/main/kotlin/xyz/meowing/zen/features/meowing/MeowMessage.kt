package xyz.meowing.zen.features.meowing

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.features.Feature
import kotlin.random.Random

@Module
object MeowMessage : Feature(
    "meowMessage",
    "Cat speak",
    "Adds random meows and purrs to your messages :3",
    "Meowing"
) {
    private val variants = listOf("meow", "mew", "mrow", "nyaa", "purr", "mrrp", "meoww", "nya")

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