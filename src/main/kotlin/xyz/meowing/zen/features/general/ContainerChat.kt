package xyz.meowing.zen.features.general

import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.components.EditBox
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.input.KnitKey
import xyz.meowing.knit.api.input.KnitKeyboard
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.MouseEvent
import xyz.meowing.zen.features.Feature

//#if MC >= 1.21.9
//$$ import net.minecraft.client.input.CharacterEvent
//$$ import net.minecraft.client.input.KeyEvent
//#endif

/**
 * Inspired from Synthesis's feature "ContainerChat"
 *
 * Synthesis's impl: [GitHub](https://github.com/SynthesisMod/Synthesis/blob/main/src/main/java/com/luna/synthesis/features/utilities/ContainerChat.java)
 */
@Module
object ContainerChat : Feature(
    "containerChat",
    "Container chat",
    "Allows you to open the chat in any container",
    "General"
) {
    private val transfer by config.switch("Remember text", true)
    private val reopenChat by config.switch("Reopen chat", true)
    private val requireCtrl by config.switch("Require ctrl", true)

    private var inputField: EditBox? = null
    private var historyBuffer = ""
    private var sentHistoryCursor = -1

    override fun initialize() {
        register<GuiEvent.Open> {
            val chatHud = client.gui?.chat ?: return@register
            sentHistoryCursor = chatHud.recentChat.size

            if (reopenChat && inputField?.isFocused == true) {
                if (client.screen is AbstractContainerScreen<*>) {
                    //#if MC >= 1.21.9
                    //$$ client.setScreen(ChatScreen(inputField?.value ?: "", true))
                    //#else
                    client.setScreen(ChatScreen(inputField?.value ?: ""))
                    //#endif
                }
            }
        }

        register<GuiEvent.Key> { event ->
            if (!KnitKey(event.key).isPressed) return@register
            if (event.screen !is AbstractContainerScreen<*>) return@register
            val field = inputField ?: return@register

            when {
                event.key == GLFW.GLFW_KEY_ESCAPE && field.isFocused -> {
                    field.isFocused = false
                    field.value = ""
                    event.cancel()
                }

                event.key == GLFW.GLFW_KEY_T && !field.isFocused-> {
                    val ctrlPressed = KnitKeyboard.isCtrlKeyPressed
                    if ((requireCtrl && ctrlPressed) || (!requireCtrl && !ctrlPressed)) {
                        field.isFocused = true
                        event.cancel()
                    }
                }

                event.key == GLFW.GLFW_KEY_SLASH && !field.isFocused -> {
                    field.value = "/"
                    field.isFocused = true
                    event.cancel()
                }

                event.key == GLFW.GLFW_KEY_ENTER && field.isFocused -> {
                    val text = field.value.trim()
                    if (text.isNotEmpty()) {
                        KnitChat.sendMessage(text)
                        sentHistoryCursor = client.gui?.chat?.recentChat?.size ?: 0
                    }
                    field.value = ""
                    field.isFocused = false
                    client.gui?.chat?.resetChatScroll()
                    event.cancel()
                }

                event.key == GLFW.GLFW_KEY_UP && field.isFocused -> {
                    navigateHistory(-1)
                    event.cancel()
                }

                event.key == GLFW.GLFW_KEY_DOWN && field.isFocused -> {
                    navigateHistory(1)
                    event.cancel()
                }

                field.isFocused -> {
                    //#if MC >= 1.21.9
                    //$$ field.keyPressed(KeyEvent(event.key, event.scanCode, 0))
                    //$$ field.charTyped(CharacterEvent(event.character.code, 0))
                    //#else
                    field.keyPressed(event.key, event.scanCode, 0)
                    field.charTyped(event.character, 0)
                    //#endif

                    event.cancel()
                }
            }
        }

        register<MouseEvent.Scroll> { event ->
            if (inputField?.isFocused != true) return@register

            var scroll = event.vertical.toInt()
            if (scroll > 1) scroll = 1
            if (scroll < -1) scroll = -1
            if (!KnitKeyboard.isShiftKeyPressed) scroll *= 7

            client.gui?.chat?.scrollChat(scroll)
        }
    }

    private fun navigateHistory(direction: Int) {
        val chatHud = client.gui?.chat ?: return
        val history = chatHud.recentChat
        val field = inputField ?: return

        val newCursor = (sentHistoryCursor + direction).coerceIn(0, history.size)

        if (newCursor != sentHistoryCursor) {
            when {
                newCursor == history.size -> {
                    sentHistoryCursor = history.size
                    field.value = historyBuffer
                }
                else -> {
                    if (sentHistoryCursor == history.size) historyBuffer = field.value
                    field.value = history[newCursor]
                    sentHistoryCursor = newCursor
                }
            }
        }
    }

    fun createInputField(screen: AbstractContainerScreen<*>): EditBox {
        val field = EditBox(client.font, 4, screen.height - 12, screen.width - 8, 12, null)
        field.setMaxLength(256)
        field.isBordered = false

        if (transfer && inputField?.isFocused == true) {
            field.value = inputField?.value ?: ""
            field.isFocused = true
        }

        inputField = field
        return field
    }

    fun shouldDrawInput(): Boolean = inputField?.isFocused == true
}