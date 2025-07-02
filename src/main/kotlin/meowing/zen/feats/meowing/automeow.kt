package meowing.zen.feats.meowing

import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import kotlin.random.Random
import net.minecraft.client.MinecraftClient

object automeow : Feature("automeow") {
    private val regex = "^(?:(Guild|Party|Officer|Co-op) > |From )?([A-Za-z0-9_]+)(?:\\s\\[[^]]+])?\\s*(?:>|:)\\s*(?:[A-Za-z0-9_]+\\s*(?:>|:)\\s*)?meow$".toRegex(RegexOption.IGNORE_CASE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Guild >" to "gc",
        "Party >" to "pc",
        "Officer >" to "oc",
        "Co-op >" to "cc"
    )

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.overlay) return@register
            val content = event.message.string.removeFormatting()

            val matchResult = regex.find(content) ?: return@register

            val playerName = MinecraftClient.getInstance().player?.name?.string ?: return@register
            val username = matchResult.groupValues[2]

            if (content.contains("To ") || username == playerName) return@register
            if (content.startsWith("G >") || content.startsWith("P >")) return@register
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