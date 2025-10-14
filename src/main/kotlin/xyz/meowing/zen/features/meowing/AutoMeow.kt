package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import kotlin.random.Random

@Zen.Module
object AutoMeow : Feature("automeow") {
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Guild >" to ("gc" to 0),
        "Party >" to ("pc" to 1),
        "Officer >" to ("oc" to 2),
        "Co-op >" to ("cc" to 3),
        "From " to ("r" to 4)
    )
    private val automeowchannels by ConfigDelegate<Set<Int>>("automeowchannels")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Auto meow", "Auto Meow", "Meowing", xyz.meowing.zen.ui.ConfigElement(
                "automeow",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Replies to messages in chat with a random meow", "", xyz.meowing.zen.ui.ConfigElement(
                "",
                ElementType.TextParagraph("Replies to messages in chat with a random meow")
            ))
            .addFeatureOption("Auto Meow Response Channels", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "automeowchannels",
                ElementType.MultiCheckbox(
                    options = listOf("Guild", "Party", "Officer", "Co-op", "Private Messages"),
                    default = setOf(0, 1, 2, 3, 4)
                )
            ))
        return configUI
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()

            if (text.contains(player?.name!!.string) || !text.endsWith("meow")) return@register

            val (cmd, channelIndex) = channels.entries.firstOrNull { text.startsWith(it.key) }?.value ?: ("ac" to -1)

            if (channelIndex !in automeowchannels) return@register

            TickUtils.schedule(Random.nextLong(10, 50)) {
                ChatUtils.command("$cmd ${meows.random()}")
            }
        }
    }
}