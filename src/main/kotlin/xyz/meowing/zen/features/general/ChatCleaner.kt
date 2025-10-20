package xyz.meowing.zen.features.general

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.constraint.ChildHeightConstraint
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.GuiEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.mixins.AccessorChatHud
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.screen.ChatScreen
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.input.KnitKey
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.zen.Zen.Companion.LOGGER
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.events.KeyEvent
import java.awt.Color
import java.util.regex.Pattern

enum class ChatFilterType { REGEX, EQUALS, CONTAINS }

data class ChatPattern(
    var pattern: String,
    val filterType: ChatFilterType
) {
    fun matches(message: String): Boolean {
        return when (filterType) {
            ChatFilterType.CONTAINS -> message.contains(pattern)
            ChatFilterType.EQUALS -> message == pattern
            ChatFilterType.REGEX -> try { message.matches(pattern.toRegex()) } catch (_: Exception) { false }
        }
    }
}

data class ChatPatterns(val patterns: MutableList<ChatPattern> = mutableListOf())

@Zen.Module
object ChatCleaner : Feature("chatcleaner") {
    private val chatcleanerkey by ConfigDelegate<Int>("chatcleanerkey")
    val patterns get() = dataUtils.getData().patterns
    val dataUtils = DataUtils("chatcleaner", ChatPatterns())

    override fun addConfig() {
        ConfigManager
            .addFeature("Chat Cleaner", "", "General", ConfigElement(
                "chatcleaner",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Keybind to add message to filter", "Keybind to add message to filter", "Options", ConfigElement(
                    "chatcleanerkey",
                    ElementType.Keybind(GLFW.GLFW_KEY_H)
            ))
            .addFeatureOption("Chat Cleaner Filter GUI", "Chat Cleaner Filter GUI", "GUI", ConfigElement(
                "chatcleanergui",
                ElementType.Button("Open Filter GUI") {
                    TickUtils.schedule(2) {
                        client.setScreen(ChatCleanerGui())
                    }
                }
            ))
    }


    init {
        loadDefault()
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val message = event.message.string.removeFormatting()
            if (patterns.any { it.matches(message) }) event.cancel()
        }

        register<KeyEvent.Press> { _ ->
            if (client.currentScreen !is ChatScreen || !KnitKey(chatcleanerkey).isPressed) return@register

            val chat = client.inGameHud.chatHud as AccessorChatHud
            val line = chat.getMessageLineIdx(chat.toChatLineMX(KnitMouse.Scaled.x), chat.toChatLineMY(KnitMouse.Scaled.y))

            if (line >= 0 && line < chat.visibleMessages.size && line < chat.messages.size) {
                val text = chat.messages[line].content().string.removeFormatting()

                if (text.isNotEmpty()) {
                    addPattern(text, ChatFilterType.EQUALS)
                    KnitChat.fakeMessage("$prefix §fAdded §7\"§c$text§7\" §fto filter.")
                }
            }
        }
    }

    fun loadDefault() {
        if (patterns.isEmpty()) {
            try {
                javaClass.getResourceAsStream("/assets/zen/chatfilter.json")?.use { stream ->
                    val defaultPatterns = com.google.gson.Gson().fromJson(
                        stream.bufferedReader().readText(), Array<String>::class.java
                    )
                    patterns.addAll(defaultPatterns.map { ChatPattern(it, ChatFilterType.REGEX) })
                    dataUtils.save()
                }
            } catch (e: Exception) {
                LOGGER.warn("Caught error while trying to load defaults in ChatCleaner: $e")
            }
        }
    }

    fun addPattern(pattern: String, filterType: ChatFilterType): Boolean {
        if (pattern.isBlank() || patterns.any { it.pattern == pattern && it.filterType == filterType }) return false
        return try {
            if (filterType == ChatFilterType.REGEX) Pattern.compile(pattern)
            patterns.add(ChatPattern(pattern, filterType))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removePattern(index: Int): Boolean {
        if (index < 0 || index >= patterns.size) return false
        patterns.removeAt(index)
        return true
    }

    fun clearAllPatterns() {
        patterns.clear()
    }

    fun updatePattern(index: Int, newPattern: String, filterType: ChatFilterType): Boolean {
        if (index < 0 || index >= patterns.size || newPattern.isBlank()) return false
        return try {
            if (filterType == ChatFilterType.REGEX) Pattern.compile(newPattern)
            patterns[index] = ChatPattern(newPattern, filterType)
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Zen.Command
object ChatCleanerCommand : Commodore("chatcleaner", "zencc", "zenchatcleaner") {
    init {
        runs {
            TickUtils.schedule(2) {
                client.setScreen(ChatCleanerGui())
            }
        }
    }
}

class ChatCleanerText(
    initialValue: String = "",
    placeholder: String = "",
    var onChange: ((String) -> Unit)? = null
) : UIContainer() {
    var text: String = initialValue
    val input: UITextInput
    private val placeholderText: UIText?
    private var onInputCallback: ((String) -> Unit)? = null

    init {
        val container = UIRoundedRectangle(3f).constrain {
            x = 1.pixels()
            y = 1.pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(Color(18, 24, 28, 255)) childOf this

        input = (UITextInput(text).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 16.pixels()
            height = 10.pixels()
        }.setColor(Color(170, 230, 240, 255)) childOf container) as UITextInput

        placeholderText = (if (placeholder.isNotEmpty()) {
            UIText(placeholder).constrain {
                x = 8.pixels()
                y = CenterConstraint()
            }.setColor(Color(80, 120, 140, 255)) childOf container
        } else null) as UIText?

        updatePlaceholderVisibility()
        setupEventHandlers()
    }

    private fun setupEventHandlers() {
        onMouseClick {
            input.setText(text)
            input.grabWindowFocus()
        }

        input.onKeyType { _, _ ->
            text = input.getText()
            updatePlaceholderVisibility()
            onChange?.invoke(text)
            onInputCallback?.invoke(text)
        }

        input.onFocusLost {
            text = input.getText()
            onChange?.invoke(text)
        }
    }

    private fun updatePlaceholderVisibility() {
        placeholderText?.let { placeholder ->
            if (text.isEmpty()) placeholder.unhide(true)
            else placeholder.hide(true)
        }
    }
}

class ChatCleanerGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(12, 16, 20, 255)
        val accent = Color(100, 245, 255, 255)
        val accent2 = Color(80, 200, 220, 255)
        val success = Color(47, 102, 47, 255)
        val danger = Color(115, 41, 41, 255)
        val buttonGroup = Color(16, 20, 24, 255)
        val buttonSelected = Color(70, 180, 200, 255)
        val buttonHover = Color(20, 70, 75, 255)
        val divider = Color(30, 35, 40, 255)
    }

    private lateinit var scrollComponent: ScrollComponent
    private lateinit var listContainer: UIContainer
    private lateinit var inputField: ChatCleanerText

    init {
        buildGui()
    }

    override fun onScreenClose() {
        super.onScreenClose()
        ChatCleaner.dataUtils.save()
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 70.percent()
            height = 80.percent()
        }.setColor(theme.accent2) childOf window

        val main = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.bg) childOf border

        createHeader(main)
        createContent(main)
        createFooter(main)
        renderPatterns()
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 40.pixels()
        } childOf parent

        UIText("§lChat Cleaner").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.8.pixels()
        }.setColor(theme.accent) childOf header

        val clearAllButton = createBlock(3f).constrain {
            x = 100.percent() - 70.pixels()
            y = CenterConstraint()
            width = 62.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf header

        clearAllButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            ChatCleaner.clearAllPatterns()
            renderPatterns()
        }

        UIText("Clear All").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(theme.accent) childOf clearAllButton

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf header
    }

    private fun createContent(parent: UIComponent) {
        val contentPanel = UIContainer().constrain {
            x = 8.pixels()
            y = 48.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 96.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        listContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(4f)
        } childOf scrollComponent
    }

    private fun createFooter(parent: UIComponent) {
        val footer = UIContainer().constrain {
            x = 8.pixels()
            y = 100.percent() - 40.pixels()
            width = 100.percent() - 16.pixels()
            height = 40.pixels()
        } childOf parent

        createBlock(0f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf footer

        inputField = ChatCleanerText("", "Enter pattern...").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 80.pixels()
            height = 24.pixels()
        } childOf footer

        inputField.input.onKeyType { _, keyCode ->
            if (keyCode == UKeyboard.KEY_ENTER) addPattern()
        }

        val addButton = createBlock(3f).constrain {
            x = 100.percent() - 64.pixels()
            y = CenterConstraint()
            width = 56.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf footer

        addButton.onMouseEnter {
            if (inputField.text.isEmpty()) {
                animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
                return@onMouseEnter
            }
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.success.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            addPattern()
            inputField.input.setText("")
        }

        UIText("Add").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(theme.accent) childOf addButton
    }

    private fun renderPatterns() {
        listContainer.clearChildren()
        val patterns = ChatCleaner.patterns

        if (patterns.isEmpty()) {
            UIText("No patterns added...").constrain {
                x = CenterConstraint()
                y = 20.pixels()
                textScale = 1f.pixels()
            }.setColor(theme.accent2.withAlpha(128)) childOf listContainer
            return
        }

        patterns.forEachIndexed { index, pattern ->
            createPatternRow(index, pattern)
        }
    }

    private fun createPatternRow(index: Int, pattern: ChatPattern) {
        val row = createBlock(3f).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(4f)
            width = 100.percent()
            height = 36.pixels()
        }.setColor(theme.element) childOf listContainer

        val textInput = ChatCleanerText(pattern.pattern).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 60.percent()
            height = 24.pixels()
        } childOf row

        textInput.onChange = { text ->
            updatePattern(index, text, pattern.filterType)
        }

        createFilterTypeButtons(row, index, pattern)
        createDeleteButton(row, index)
    }

    private fun createFilterTypeButtons(parent: UIComponent, index: Int, pattern: ChatPattern) {
        val container = createBlock(2f).constrain {
            x = 63.percent()
            y = CenterConstraint()
            width = 30.percent()
            height = 20.pixels()
        }.setColor(theme.buttonGroup) childOf parent

        val options = listOf(
            ChatFilterType.CONTAINS to "Contains",
            ChatFilterType.EQUALS to "Equals",
            ChatFilterType.REGEX to "Regex"
        )

        options.forEachIndexed { optionIndex, (type, name) ->
            createButtonGroupOption(
                name,
                pattern.filterType == type,
                optionIndex == 0,
                optionIndex == options.size - 1
            ) {
                updatePattern(index, pattern.pattern, type)
            }.constrain {
                x = (optionIndex * 33.33).percent()
                y = 0.percent()
                width = 33.33.percent()
                height = 100.percent()
            } childOf container

            if (optionIndex < options.size - 1) {
                createBlock(0f).constrain {
                    x = ((optionIndex + 1) * 33.33).percent()
                    y = 2.pixels()
                    width = 1.pixels()
                    height = 100.percent() - 4.pixels()
                }.setColor(theme.divider) childOf container
            }
        }
    }

    private fun createButtonGroupOption(
        text: String,
        selected: Boolean,
        isFirst: Boolean,
        isLast: Boolean,
        onClick: () -> Unit
    ): UIComponent {
        val radius = when {
            isFirst -> 2f
            isLast -> 2f
            else -> 0f
        }

        val buttonBorder = createBlock(radius).setColor(theme.buttonSelected)

        val button = createBlock(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
            color = theme.buttonGroup.constraint
        } childOf buttonBorder

        buttonBorder.setColor(if (selected) theme.buttonSelected else Color(0,0,0,0))

        if (!selected) {
            button.onMouseEnter {
                buttonBorder.animate { setColorAnimation(Animations.OUT_EXP, 0.2f, theme.buttonHover.toConstraint()) }
            }.onMouseLeave {
                buttonBorder.animate { setColorAnimation(Animations.OUT_EXP, 0.2f, Color(0,0,0,0).toConstraint()) }
            }
        }

        button.onMouseClick {
            if (!selected) onClick()
        }

        UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(if (selected) Color.WHITE else theme.accent) childOf button

        return buttonBorder
    }

    private fun createDeleteButton(parent: UIComponent, index: Int) {
        val deleteButton = createBlock(3f).constrain {
            x = 100.percent() - 28.pixels()
            y = CenterConstraint()
            width = 20.pixels()
            height = 20.pixels()
        }.setColor(theme.element) childOf parent

        deleteButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick { removePattern(index) }

        UIText("✕").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(Color.RED.darker()) childOf deleteButton
    }

    private fun addPattern() {
        val pattern = inputField.text.trim()
        if (pattern.isEmpty()) {
            KnitChat.fakeMessage("§cEnter a pattern!")
            return
        }

        if (ChatCleaner.addPattern(pattern, ChatFilterType.CONTAINS)) {
            inputField.text = ""
            renderPatterns()
        }
    }

    private fun updatePattern(index: Int, text: String, filterType: ChatFilterType) {
        if (ChatCleaner.updatePattern(index, text, filterType)) {
            renderPatterns()
        }
    }

    private fun removePattern(index: Int) {
        if (ChatCleaner.removePattern(index)) {
            renderPatterns()
        }
    }

    private fun Color.withAlpha(alpha: Int): Color = Color(red, green, blue, alpha)
}