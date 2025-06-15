package meowing.zen.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object chatutils {
    @JvmStatic
    fun clientmsg(message: String, addHud: Boolean) {
        if (addHud) {
            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.literal(message))
        } else {
            MinecraftClient.getInstance().player?.sendMessage(Text.literal(message), false)
        }
    }

    @JvmStatic
    fun sendmsg(message: String) {
        MinecraftClient.getInstance().player?.networkHandler?.sendChatMessage(message)
    }

    @JvmStatic
    fun sendcmd(command: String) {
        MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand(command)
    }

    @JvmStatic
    fun removeFormatting(text: String): String {
        return text.replace(Regex("§[0-9a-fk-or]"), "")
    }
}