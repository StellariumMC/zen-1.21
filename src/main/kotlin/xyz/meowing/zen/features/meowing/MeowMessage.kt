package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import kotlin.random.Random

@Zen.Module
object MeowMessage : Feature("meowmessage") {
    private val variants = listOf("meow", "mew", "mrow", "nyaa", "purr", "mrrp", "meoww", "nya")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Cat Speak", "Cat Speak", "Meowing", xyz.meowing.zen.ui.ConfigElement(
                "meowmessage",
                ElementType.Switch(false)
            ))
        return configUI
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