package xyz.meowing.zen.features.general.chatCleaner

import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateFloat
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.core.VexelScreen
import xyz.meowing.vexel.elements.Button
import xyz.meowing.vexel.elements.TextInput
import xyz.meowing.zen.ui.Theme
import kotlin.math.max

class ChatCleanerGui : VexelScreen("Chat Cleaner") {
    private lateinit var listContainer: Container
    private lateinit var inputField: TextInput
    private var isAnimating = false
    private val patternRows = mutableMapOf<Int, PatternRow>()

    data class PatternRow(
        val container: Rectangle,
        val textInput: TextInput,
        val buttons: List<Button>
    )

    override fun afterInitialization() {
        val mainContainer = Rectangle(Theme.BgDark.color, Theme.Border.color, 8f, 1f)
            .setSizing(70f, Size.ParentPerc, 80f, Size.ParentPerc)
            .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
            .padding(16f)
            .dropShadow(20f, 2f, Theme.BgDark.color)
            .childOf(window)

        createHeader(mainContainer)
        createContent(mainContainer)
        createFooter(mainContainer)
        renderPatterns()
    }

    override fun onCloseGui() {
        super.onCloseGui()
        ChatCleaner.patternData.forceSave()
    }

    override fun onRenderGui() {
        if (!isAnimating) adjustScrollAfterResize()
    }

    private fun createHeader(parent: Rectangle) {
        val header = Container()
            .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(parent)

        Text("Chat Cleaner", Theme.Text.color, 24f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .setOffset(0f, -8f)
            .childOf(header)

        Button("Clear All", Theme.Text.color, fontSize = 12f)
            .setSizing(80f, Size.Pixels, 32f, Size.Pixels)
            .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
            .setOffset(0f, -8f)
            .alignRight()
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                ChatCleaner.clearAllPatterns()
                renderPatterns()
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.Danger.color, 150L) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color, 150L) }
                )
            }
            .childOf(header)

        Rectangle(Theme.Border.color, 0, 0f, 0f)
            .setSizing(100f, Size.ParentPerc, 1f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 4f, Pos.AfterSibling)
            .childOf(header)
    }

    private fun createContent(parent: Rectangle) {
        listContainer = Container(floatArrayOf(0f, 0f, 12f, 0f), scrollable = true)
            .setSizing(100f, Size.ParentPerc, 87.5f, Size.Fill)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .scrollbarColor(Theme.Highlight.color)
            .scrollbarRadius(4f)
            .scrollbarIgnorePadding(true)
            .scrollbarCustomPadding(-10f)
            .childOf(parent)
    }

    private fun createFooter(parent: Rectangle) {
        Rectangle(Theme.Border.color, 0, 0f, 0f)
            .setSizing(100f, Size.ParentPerc, 1f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(parent)

        val footer = Container(floatArrayOf(12f, 0f, 0f, 0f))
            .setSizing(100f, Size.ParentPerc, 48f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(parent)

        inputField = TextInput("", "Enter pattern...", hoverColor = null)
            .setSizing(90f, Size.ParentPerc, 36f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .fontSize(13f)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .padding(10f)
            .childOf(footer)
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.BgLight.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color) }
                )
            }

        inputField.onCharType { keyCode, _, _ ->
            if (keyCode == KnitKeys.KEY_ENTER.code) {
                addPattern()
                true
            } else false
        }

        Button("Add", 0xFFFFFFFF.toInt(), fontSize = 13f)
            .setSizing(0f, Size.Fill, 36f, Size.Pixels)
            .setPositioning(8f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.Primary.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                addPattern()
                true
            }
            .childOf(footer)
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.Highlight.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Primary.color) }
                )
            }

    }

    private fun renderPatterns() {
        listContainer.children.toList().forEach { it.destroy() }
        patternRows.clear()

        if (ChatCleaner.patterns.isEmpty()) {
            Text("No patterns added yet", Theme.TextMuted.color, 14f)
                .setPositioning(0f, Pos.ParentCenter, 40f, Pos.ParentPixels)
                .childOf(listContainer)
            return
        }

        ChatCleaner.patterns.forEachIndexed { index, pattern ->
            createPatternRow(index, pattern)
        }
    }

    private fun createPatternRow(index: Int, pattern: ChatPattern) {
        val row = Rectangle(Theme.Bg.color, Theme.Border.color, 6f, 1f)
            .setSizing(100f, Size.ParentPerc, 48f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, if (index == 0) 0f else 8f, Pos.AfterSibling)
            .padding(12f)
            .childOf(listContainer)

        val textInput = TextInput(pattern.pattern, "Pattern", hoverColor = null)
            .setSizing(0f, Size.Fill, 28f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .fontSize(12f)
            .backgroundColor(Theme.BgLight.color)
            .borderColor(Theme.BorderMuted.color)
            .borderRadius(4f)
            .padding(8f)
            .childOf(row)
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.Bg.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.BgLight.color) }
                )
            }

        textInput.onValueChange { text ->
            ChatCleaner.updatePattern(index, text as String, pattern.filterType)
        }

        val buttons = createFilterButtons(row, index, pattern)
        createDeleteButton(row, index)

        patternRows[index] = PatternRow(row, textInput, buttons)
    }

    private fun createFilterButtons(parent: Rectangle, index: Int, pattern: ChatPattern): List<Button> {
        val container = Container(padding = floatArrayOf(0f, 0f, 0f, 8f))
            .setSizing(220f, Size.Auto, 28f, Size.Pixels)
            .setPositioning(0f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .childOf(parent)

        val types = listOf(
            ChatFilterType.CONTAINS to "Contains",
            ChatFilterType.EQUALS to "Equals",
            ChatFilterType.REGEX to "Regex"
        )

        val buttons = mutableListOf<Button>()

        types.forEachIndexed { i, (type, name) ->
            val isSelected = pattern.filterType == type

            val button = Button(name, if (isSelected) 0xFFFFFFFF.toInt() else Theme.TextMuted.color, fontSize = 11f)
                .setSizing(65f, Size.Pixels, 28f, Size.Pixels)
                .setPositioning(if (i == 0) 0f else 5f, if (i == 0) Pos.ParentPixels else Pos.AfterSibling, 0f, Pos.ParentPixels)
                .backgroundColor(if (isSelected) Theme.Highlight.color else Theme.BgLight.color)
                .borderColor(Theme.BorderMuted.color)
                .borderRadius(4f)
                .hoverColors(bg = Theme.Primary.color, text = 0xFFFFFFFF.toInt())
                .padding(6f)
                .onClick { _, _, _ ->
                    updatePatternType(index, type)
                    true
                }
                .childOf(container)

            buttons.add(button)
        }

        return buttons
    }

    private fun createDeleteButton(parent: Rectangle, index: Int) {
        Button("×", Theme.Danger.color, fontSize = 18f)
            .setSizing(32f, Size.Pixels, 28f, Size.Pixels)
            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.BgLight.color)
            .borderColor(Theme.BorderMuted.color)
            .borderRadius(4f)
            .hoverColors(null, Theme.Text.color)
            .padding(4f)
            .onClick { _, _, _ ->
                if (ChatCleaner.removePattern(index)) renderPatterns()
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ ->
                        this.background.colorTo(Theme.Danger.color, 150L)
                    },
                    onExit = { _, _ ->
                        this.background.colorTo(Theme.BgLight.color, 150L)
                    }
                )
            }
            .childOf(parent)
    }

    private fun addPattern() {
        val pattern = inputField.value.trim()
        if (pattern.isEmpty()) {
            KnitChat.fakeMessage("§cEnter a pattern!")
            return
        }

        if (ChatCleaner.addPattern(pattern, ChatFilterType.CONTAINS)) {
            inputField.value = ""
            renderPatterns()
        }
    }

    private fun updatePatternType(index: Int, filterType: ChatFilterType) {
        val pattern = ChatCleaner.patterns.getOrNull(index) ?: return
        if (ChatCleaner.updatePattern(index, pattern.pattern, filterType)) {
            val row = patternRows[index] ?: return
            val types = listOf(ChatFilterType.CONTAINS, ChatFilterType.EQUALS, ChatFilterType.REGEX)

            row.buttons.forEachIndexed { i, button ->
                val isSelected = types[i] == filterType
                val bgColor = if (isSelected) Theme.Highlight.color else Theme.BgLight.color

                button.isHovered = false
                button.isPressed = false

                button.backgroundColor(bgColor)
                button.textColor(if (isSelected) 0xFFFFFFFF.toInt() else Theme.TextMuted.color)
                button.hoverColor(Theme.Primary.color)
            }
        }
    }

    private fun adjustScrollAfterResize() {
        val contentHeight = listContainer.getContentHeight()
        val viewHeight = listContainer.height - listContainer.padding[0] - listContainer.padding[2]
        val maxScroll = max(0f, contentHeight - viewHeight)

        if (listContainer.scrollOffset > maxScroll) {
            isAnimating = true
            listContainer.animateFloat(
                { listContainer.scrollOffset },
                { listContainer.scrollOffset = it },
                maxScroll,
                100,
                EasingType.EASE_OUT,
                onComplete = {
                    isAnimating = false
                }
            )
        }
    }
}