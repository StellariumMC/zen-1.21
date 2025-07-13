package meowing.zen.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import meowing.zen.Zen.Companion.mc
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.util.concurrent.ConcurrentLinkedQueue

object ChatUtils {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val queue = ConcurrentLinkedQueue<String>()
    private var lastSent = 0L
    private var processing = false

    fun chat(message: String) {
        val player = mc.player ?: return
        val now = System.currentTimeMillis()

        if (now - lastSent >= 100 && !processing) {
            player.networkHandler.sendChatMessage(message)
            lastSent = now
        } else {
            queue.offer(message)
            if (!processing) processQueue()
        }
    }

    private fun processQueue() {
        processing = true
        scope.launch {
            while (queue.isNotEmpty()) {
                val elapsed = System.currentTimeMillis() - lastSent
                if (elapsed < 100) {
                    delay(100 - elapsed)
                }

                queue.poll()?.let { message ->
                    val player = mc.player
                    if (player != null) {
                        player.networkHandler.sendChatMessage(message)
                        lastSent = System.currentTimeMillis()
                    }
                }
            }
            processing = false
        }
    }

    fun command(command: String) {
        val player = mc.player ?: return
        val cmd = if (command.startsWith("/")) command else "/$command"
        player.networkHandler.sendChatCommand(cmd.substring(1))
    }

    fun addMessage(
        message: String,
        hover: String? = null,
        clickAction: ClickEvent.Action? = null,
        clickValue: String? = null,
        siblingText: String? = null
    ) {
        val component = Text.literal(message) as MutableText

        siblingText?.let { text ->
            val sibling = Text.literal(text).apply {
                style = createStyle(hover, clickAction, clickValue)
            }
            component.append(sibling)
        } ?: run {
            component.style = createStyle(hover, clickAction, clickValue)
        }

        mc.inGameHud.chatHud.addMessage(component)
    }

    fun createStyle(hover: String?, clickAction: ClickEvent.Action?, clickValue: String?): Style {
        var style = Style.EMPTY

        hover?.let {
            style = style.withHoverEvent(HoverEvent.ShowText(Text.literal(it)))
        }

        if (clickAction != null && clickValue != null) {
            val clickEvent = when (clickAction) {
                ClickEvent.Action.RUN_COMMAND -> ClickEvent.RunCommand(clickValue)
                ClickEvent.Action.SUGGEST_COMMAND -> ClickEvent.SuggestCommand(clickValue)
                ClickEvent.Action.OPEN_URL -> ClickEvent.OpenUrl(java.net.URI.create(clickValue))
                ClickEvent.Action.OPEN_FILE -> ClickEvent.OpenFile(clickValue)
                ClickEvent.Action.CHANGE_PAGE -> ClickEvent.ChangePage(clickValue.toIntOrNull() ?: 1)
                ClickEvent.Action.COPY_TO_CLIPBOARD -> ClickEvent.CopyToClipboard(clickValue)
            }
            style = style.withClickEvent(clickEvent)
        }

        return style
    }

    private data class Threshold(val value: Double, val symbol: String, val precision: Int)
    private val thresholds = listOf(Threshold(1e9, "b", 1), Threshold(1e6, "m", 1), Threshold(1e3, "k", 1))

    fun formatNumber(number: String): String {
        return try {
            val num = number.replace(",", "").toDouble()
            val threshold = thresholds.find { num >= it.value }

            if (threshold != null) {
                val formatted = num / threshold.value
                val rounded = String.format("%.${threshold.precision}f", formatted).toDouble()
                "${rounded}${threshold.symbol}"
            } else {
                if (num == num.toLong().toDouble()) num.toLong().toString()
                else num.toString()
            }
        } catch (e: NumberFormatException) {
            number
        }
    }

    fun toLegacyString(text: Text?): String {
        if (text == null) return ""
        val builder = StringBuilder()

        fun append(component: Text?) {
            component!!.style.color?.let { color ->
                builder.append("§${
                    when(color.name) {
                        "black" -> "0"
                        "dark_blue" -> "1"
                        "dark_green" -> "2"
                        "dark_aqua" -> "3"
                        "dark_red" -> "4"
                        "dark_purple" -> "5"
                        "gold" -> "6"
                        "gray" -> "7"
                        "dark_gray" -> "8"
                        "blue" -> "9"
                        "green" -> "a"
                        "aqua" -> "b"
                        "red" -> "c"
                        "light_purple" -> "d"
                        "yellow" -> "e"
                        "white" -> "f"
                        else -> "f"
                    }
                }")
            }

            with(component.style) {
                if (isBold) builder.append("§l")
                if (isItalic) builder.append("§o")
                if (isUnderlined) builder.append("§n")
                if (isStrikethrough) builder.append("§m")
                if (isObfuscated) builder.append("§k")
            }

            val content = component.string
            if (content.isNotEmpty() && component.siblings.isEmpty()) builder.append(content)
            component.siblings.forEach { append(it) }
        }

        append(text)
        return builder.toString()
    }
}