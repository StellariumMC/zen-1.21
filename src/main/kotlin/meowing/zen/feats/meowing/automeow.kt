package meowing.zen.feats.meowing

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import kotlin.random.Random

object automeow {
    private val meowRegex = Regex("^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$", RegexOption.IGNORE_CASE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val meowmap = mapOf(
        "Party >" to "pc",
        "Guild >" to "gc",
        "Officer >" to "oc",
        "Co-op >" to "cc"
    )

    fun initialize() {
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            if (!overlay && meowRegex.matches(message.string)) {
                val content = message.string
                val playerName = MinecraftClient.getInstance().player?.name?.string

                if (content.contains("To ") || content.contains("From $playerName")) return@register
                Thread.sleep(Random.nextLong(500, 2500))

                val cmd = when {
                    content.startsWith("From ") -> {
                        val user = meowRegex.find(content)?.groupValues?.get(1)
                        "msg $user"
                    }
                    else -> {
                        val channelMatch = meowmap.keys.find { content.startsWith(it) }
                        channelMatch?.let { meowmap[it] } ?: "ac"
                    }
                }

                val randomMeow = meows[Random.nextInt(meows.size)]
                MinecraftClient.getInstance().networkHandler?.sendChatCommand("$cmd $randomMeow")
            }
        }
    }
}