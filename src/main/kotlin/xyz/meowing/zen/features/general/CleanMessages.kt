package xyz.meowing.zen.features.general

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Module
object GuildMessage : Feature(
    "guildMessage",
    "Clean guild messages",
    "Reformats guild messages",
    "General",
) {
    private val guildPattern = Pattern.compile("Guild > (\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?: (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val text = event.message.string.removeFormatting()
            val m = guildPattern.matcher(text)
            if (m.matches()) {
                event.cancel()
                val hrank = m.group(1) ?: ""
                val user = m.group(2) ?: ""
                val grank = m.group(3) ?: ""
                val msg = m.group(4) ?: ""
                val grankText = if (grank.isNotEmpty()) "§8$grank " else ""
                val formatted = "§2G §8> $grankText§${getRankColor(hrank)}$user§f: $msg"
                KnitChat.fakeMessage(formatted)
            }
        }
    }

    private fun getRankColor(rank: String) = when {
        rank.isEmpty() -> "7"
        else -> when (rankPattern.matcher(rank).takeIf { it.find() }?.group(1)) {
            "Admin" -> "c"
            "Mod", "GM" -> "2"
            "Helper" -> "b"
            "MVP++", "MVP+", "MVP" -> if (rank.contains("++")) "6" else "b"
            "VIP+", "VIP" -> "a"
            else -> "7"
        }
    }
}

@Module
object PartyMessage : Feature(
    "partyMessage",
    "Clean party messages",
    "Reformats party messages",
    "General",
) {
    private val partyPattern = Pattern.compile("Party > (\\[.+?])? ?(.+?): (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            val m = partyPattern.matcher(text)
            if (m.matches()) {
                event.cancel()
                val hrank = m.group(1) ?: ""
                val user = m.group(2) ?: ""
                val msg = m.group(3) ?: ""
                val formatted = "§9P §8> §${getRankColor(hrank)}$user§f: $msg"
                KnitChat.fakeMessage(formatted)
            }
        }
    }

    private fun getRankColor(rank: String) = when {
        rank.isEmpty() -> "7"
        else -> when (rankPattern.matcher(rank).takeIf { it.find() }?.group(1)) {
            "Admin" -> "c"
            "Mod", "GM" -> "2"
            "Helper" -> "b"
            "MVP++", "MVP+", "MVP" -> if (rank.contains("++")) "6" else "b"
            "VIP+", "VIP" -> "a"
            else -> "7"
        }
    }
}