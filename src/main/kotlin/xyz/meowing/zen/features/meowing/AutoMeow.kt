package xyz.meowing.zen.features.meowing

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import kotlin.random.Random

@Module
object AutoMeow : Feature(
    "autoMeow",
    "Auto meow",
    "Replies to messages in chat with a random meow",
    "Meowing"
) {
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels by config.multiCheckbox("Response channels", listOf("Guild", "Party", "Officer", "Co-op", "Private Messages"), setOf(0, 1, 2, 3, 4))
    private val map = mapOf(
        "Guild >" to ("gc" to 0),
        "Party >" to ("pc" to 1),
        "Officer >" to ("oc" to 2),
        "Co-op >" to ("cc" to 3),
        "From " to ("r" to 4)
    )

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            val player = player ?: return@register

            if (text.contains(player.name.string) || !text.endsWith("meow")) return@register

            val (cmd, channelIndex) = map.entries.firstOrNull { text.startsWith(it.key) }?.value ?: ("ac" to -1)

            if (channelIndex !in channels) return@register

            TickUtils.schedule(Random.nextLong(10, 50)) {
                KnitChat.sendCommand("$cmd ${meows.random()}")
            }
        }
    }
}