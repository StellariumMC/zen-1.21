package xyz.meowing.zen.features.general

import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.input.KnitKey
import xyz.meowing.knit.api.input.KnitKeyboard
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.MouseEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

//#if MC >= 1.21.9
//$$ import net.minecraft.client.input.CharInput
//$$ import net.minecraft.client.input.KeyInput
//#endif

/**
 * Inspired from Synthesis's feature "ContainerChat"
 *
 * Synthesis's impl: [GitHub](https://github.com/SynthesisMod/Synthesis/blob/main/src/main/java/com/luna/synthesis/features/utilities/ContainerChat.java)
 */
@Module
object ContainerChat : Feature(
    "containerChat"
) {
    private val transferText by ConfigDelegate<Boolean>("containerChat.transfer")
    private val reopenChat by ConfigDelegate<Boolean>("containerChat.reopen")
    private val requireCtrl by ConfigDelegate<Boolean>("containerChat.ctrl")

    private var inputField: TextFieldWidget? = null
    private var historyBuffer = ""
    private var sentHistoryCursor = -1

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Container chat",
                "Allows you to open the chat in any container",
                "General",
                ConfigElement(
                    "containerChat",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Remember text",
                ConfigElement(
                    "containerChat.transfer",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Reopen chat",
                ConfigElement(
                    "containerChat.reopen",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Ctrl to open chat",
                ConfigElement(
                    "containerChat.ctrl",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<GuiEvent.Open> {
            val chatHud = client.inGameHud?.chatHud ?: return@register
            sentHistoryCursor = chatHud.messageHistory.size

            if (reopenChat && inputField?.isFocused == true) {
                if (client.currentScreen is HandledScreen<*>) {
                    //#if MC >= 1.21.9
                    //$$ client.setScreen(ChatScreen(inputField?.text ?: "", true))
                    //#else
                    client.setScreen(ChatScreen(inputField?.text ?: ""))
                    //#endif
                }
            }
        }

        register<GuiEvent.Key> { event ->
            if (!KnitKey(event.key).isPressed) return@register
            if (event.screen !is HandledScreen<*>) return@register
            val field = inputField ?: return@register

            when {
                event.key == GLFW.GLFW_KEY_ESCAPE && field.isFocused -> {
                    field.isFocused = false
                    field.text = ""
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
                    field.text = "/"
                    field.isFocused = true
                    event.cancel()
                }

                event.key == GLFW.GLFW_KEY_ENTER && field.isFocused -> {
                    val text = field.text.trim()
                    if (text.isNotEmpty()) {
                        KnitChat.sendMessage(text)
                        sentHistoryCursor = client.inGameHud?.chatHud?.messageHistory?.size ?: 0
                    }
                    field.text = ""
                    field.isFocused = false
                    client.inGameHud?.chatHud?.resetScroll()
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
                    //$$ field.keyPressed(KeyInput(event.key, event.scanCode, 0))
                    //$$ field.charTyped(CharInput(event.character.digitToInt(), 0))
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

            client.inGameHud?.chatHud?.scroll(scroll)
        }
    }

    private fun navigateHistory(direction: Int) {
        val chatHud = client.inGameHud?.chatHud ?: return
        val history = chatHud.messageHistory
        val field = inputField ?: return

        val newCursor = (sentHistoryCursor + direction).coerceIn(0, history.size)

        if (newCursor != sentHistoryCursor) {
            when {
                newCursor == history.size -> {
                    sentHistoryCursor = history.size
                    field.text = historyBuffer
                }
                else -> {
                    if (sentHistoryCursor == history.size) historyBuffer = field.text
                    field.text = history[newCursor]
                    sentHistoryCursor = newCursor
                }
            }
        }
    }

    fun createInputField(screen: HandledScreen<*>): TextFieldWidget {
        val field = TextFieldWidget(client.textRenderer, 4, screen.height - 12, screen.width - 8, 12, null)
        field.setMaxLength(256)
        field.setDrawsBackground(false)

        if (transferText && inputField?.isFocused == true) {
            field.text = inputField?.text ?: ""
            field.isFocused = true
        }

        inputField = field
        return field
    }

    fun shouldDrawInput(): Boolean = inputField?.isFocused == true
}