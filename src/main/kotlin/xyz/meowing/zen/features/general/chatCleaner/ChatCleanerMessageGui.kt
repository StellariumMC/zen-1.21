package xyz.meowing.zen.features.general.chatCleaner

import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import java.util.Stack

object ChatCleanerMessageGui {
    private const val NAME = "Chat Cleaner"
    private data class MessageData(val message: String, var ticksRemaining: Int)

    private val messageList = mutableListOf<MessageData>()

    fun addNewMessage(message: String, ticks: Int = 100) {
        messageList.add(MessageData(message, ticks))
    }

    fun render(context: GuiGraphics) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        val removeStack: Stack<MessageData> = Stack()

        for(i in messageList.indices) {
            val message = messageList[i]
            message.ticksRemaining--

            if(message.ticksRemaining == 0)
                removeStack.add(message)

            Render2D.renderString(context, message.message, x, y + (i * 10 * scale), scale)
        }

        for(entry in removeStack)
            messageList.remove(entry)
    }
}