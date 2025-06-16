package meowing.zen.feats.general

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import meowing.zen.utils.chatutils
import meowing.zen.featManager
import java.util.regex.Pattern

object cleanjoin {
    private val guild = Pattern.compile("^Guild > (.+) (joined|left).")
    private val friend = Pattern.compile("^Friend > (.+) (joined|left).")

    @JvmStatic
    fun initialize() {
        featManager.register(this) {
            EventBus.register(EventTypes.GameMessageEvent::class.java, this, this::onGameMessage)
        }
    }


    private fun onGameMessage(event: EventTypes.GameMessageEvent) {
        val text = chatutils.removeFormatting(event.getPlainText())
        var m = guild.matcher(text)
        if (m.matches()) {
            val action = if ("joined" == m.group(2)) "§2>>" else "§4<<"
            chatutils.clientmsg("§8G $action §b${m.group(1)}", true)
            event.hide()
        }
        m = friend.matcher(text)
        if (m.matches()) {
            val action = if ("joined" == m.group(2)) "§2>>" else "§4<<"
            chatutils.clientmsg("§8F $action §b${m.group(1)}", true)
            event.hide()
        }
    }
}