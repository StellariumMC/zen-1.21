package meowing.zen.feats.meowing

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import net.minecraft.client.MinecraftClient
import meowing.zen.featManager
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.chatutils
import java.util.regex.Pattern
import kotlin.random.Random

object automeow {
    private val regex = Pattern.compile(
        "^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$",
        Pattern.CASE_INSENSITIVE
    )
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Party >" to "pc",
        "Guild >" to "gc",
        "Officer >" to "oc",
        "Co-op >" to "cc"
    )

    @JvmStatic
    fun initialize() {
        TickScheduler.register()
        featManager.register(this) {
            EventBus.register(EventTypes.GameMessageEvent::class.java, this, this::onGameMessage)
        }
    }

    private fun onGameMessage(event: EventTypes.GameMessageEvent) {
        if (event.overlay || !regex.matcher(chatutils.removeFormatting(event.getPlainText())).matches()) return

        val content = chatutils.removeFormatting(event.getPlainText())
        val playerName = MinecraftClient.getInstance().player?.name?.string ?: return
        if (content.contains("To ") || content.contains(playerName)) return

        TickScheduler.schedule(Random.nextLong(10, 50)) {
            val cmd = if (content.startsWith("From ")) {
                "msg " + regex.matcher(content).replaceFirst("$1")
            } else {
                channels.entries.find { content.startsWith(it.key) }?.value ?: "ac"
            }
            chatutils.sendcmd("$cmd ${meows.random()}")
        }
    }
}