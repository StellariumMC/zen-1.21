package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import kotlin.random.Random

@Zen.Module
object automeow : Feature("automeow") {
    private val regex = "^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$".toRegex(RegexOption.IGNORE_CASE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Guild >" to "gc",
        "Party >" to "pc",
        "Officer >" to "oc",
        "Co-op >" to "cc"
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Auto meow", ConfigElement(
                "automeow",
                "Auto Meow",
                "Automatically responds with a meow message whenever someone sends meow in chat.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val content = event.message.string.removeFormatting()
            val matchResult = regex.find(content) ?: return@register
            val username = matchResult.groupValues[1]

            if (content.contains("To ") || username == mc.player?.name?.string) return@register
            TickUtils.schedule(Random.nextLong(10, 50)) {
                val cmd = when {
                    content.startsWith("From ") -> {
                        "msg $username"
                    }
                    else -> channels.entries.find { content.startsWith(it.key) }?.value ?: "ac"
                }
                ChatUtils.command("$cmd ${meows.random()}")
            }
        }
    }
}