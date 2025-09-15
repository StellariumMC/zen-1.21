package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import kotlin.random.Random

@Zen.Module
object MeowMessage : Feature("meowmessage") {
    private val variants = listOf("meow", "mew", "mrow", "nyaa", "purr", "mrrp", "meoww", "nya")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Cat Speak", ConfigElement(
                "meowmessage",
                "Cat Speak",
                ElementType.Switch(false)
            ), true)
    }

    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (event.chatUtils) return@register
            event.cancel()

            if (event.message.startsWith("/")) {
                val parts = event.message.split(" ")
                if (parts.size > 1) ChatUtils.command("${parts[0]} ${transform(parts.drop(1).joinToString(" "))}")
                else ChatUtils.command(event.message)
            } else {
                ChatUtils.chat(transform(event.message))
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