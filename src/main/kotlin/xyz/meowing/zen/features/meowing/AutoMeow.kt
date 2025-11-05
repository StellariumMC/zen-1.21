package xyz.meowing.zen.features.meowing

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import kotlin.random.Random

@Module
object AutoMeow : Feature(
    "autoMeow"
) {
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Guild >" to ("gc" to 0),
        "Party >" to ("pc" to 1),
        "Officer >" to ("oc" to 2),
        "Co-op >" to ("cc" to 3),
        "From " to ("r" to 4)
    )
    private val autoMeowChannels by ConfigDelegate<Set<Int>>("autoMeow.channels")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Auto meow",
                "Replies to messages in chat with a random meow",
                "Meowing",
                ConfigElement(
                    "autoMeow",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Auto meow response channels",
                ConfigElement(
                    "autoMeow.channels",
                    ElementType.MultiCheckbox(
                        options = listOf("Guild", "Party", "Officer", "Co-op", "Private Messages"),
                        default = setOf(0, 1, 2, 3, 4)
                    )
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            val player = player ?: return@register

            if (text.contains(player.name.string) || !text.endsWith("meow")) return@register

            val (cmd, channelIndex) = channels.entries.firstOrNull { text.startsWith(it.key) }?.value ?: ("ac" to -1)

            if (channelIndex !in autoMeowChannels) return@register

            TickUtils.schedule(Random.nextLong(10, 50)) {
                KnitChat.sendCommand("$cmd ${meows.random()}")
            }
        }
    }
}