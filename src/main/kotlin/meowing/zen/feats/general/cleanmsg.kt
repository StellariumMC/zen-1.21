package meowing.zen.feats.general

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import meowing.zen.utils.chatutils
import meowing.zen.Zen
import meowing.zen.featManager
import java.util.regex.Pattern

object cleanmsg {
    private val guild = Pattern.compile("^Guild > ?(\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?: (.+)")
    private val party = Pattern.compile("^Party > (\\[.+?])? ?(.+?): (.+)")
    private val RANK_COLORS = mapOf(
        "Admin" to "§c",
        "Mod" to "§2",
        "Helper" to "§9",
        "GM" to "§2",
        "MVP++" to "§${Zen.getConfig().mvppluspluscolor}",
        "MVP+" to "§${Zen.getConfig().mvppluscolor}",
        "MVP" to "§${Zen.getConfig().mvpcolor}",
        "VIP+" to "§${Zen.getConfig().vippluscolor}",
        "VIP" to "§${Zen.getConfig().vipcolor}"
    )

    @JvmStatic
    fun initialize() {
        featManager.register(this) {
            EventBus.register(EventTypes.GameMessageEvent::class.java, this, this::onGameMessage)
        }
    }

    private fun processGuild(text: String): String? {
        val m = guild.matcher(text)
        if (!m.matches()) return null
        val grank = if (m.group(3) != null) "§8${m.group(3)} " else ""
        return "§2G §8> $grank${getRankColor(m.group(1))}${m.group(2)}§f: ${m.group(4)}"
    }

    private fun processParty(text: String): String? {
        val m = party.matcher(text)
        return if (m.matches()) {
            "§9P §8> ${getRankColor(m.group(1))}${m.group(2)}§f: ${m.group(3)}"
        } else null
    }

    private fun getRankColor(rank: String?): String {
        if (rank == null) return "§7"
        return RANK_COLORS[rank.replace(Regex("[\\[\\]]"), "")] ?: "§7"
    }

    private fun onGameMessage(event: EventTypes.GameMessageEvent) {
        val text = chatutils.removeFormatting(event.getPlainText())
        val processed = processGuild(text) ?: processParty(text)

        if (processed != null) {
            chatutils.clientmsg(processed, true)
            event.hide()
        }
    }
}