package xyz.meowing.zen.features.general.keyShortcuts

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
import xyz.meowing.zen.utils.Utils.getKeyName
import kotlin.math.max

class KeybindGui : VexelScreen("Key Shortcuts") {
    private lateinit var listContainer: Container
    private lateinit var commandField: TextInput
    private lateinit var keyButton: KeybindButton
    private var isAnimating = false
    private val bindingRows = mutableMapOf<Int, BindingRow>()

    data class BindingRow(
        val container: Rectangle,
        val keyDisplay: Rectangle,
        val commandInput: TextInput,
        val deleteButton: Button
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
        renderBindings()
    }

    override fun onCloseGui() {
        super.onCloseGui()
        KeyShortcuts.bindingData.forceSave()
    }

    override fun onRenderGui() {
        if (!isAnimating) adjustScrollAfterResize()
    }

    private fun createHeader(parent: Rectangle) {
        val header = Container()
            .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(parent)

        Text("Key Shortcuts", Theme.Text.color, 24f)
            .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
            .setOffset(0f, -8f)
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

        commandField = TextInput("", "To run (e.g., /help or hello world)", hoverColor = null)
            .setSizing(61f, Size.ParentPerc, 36f, Size.Pixels)
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

        keyButton = KeybindButton()
            .setSizing(25f, Size.ParentPerc, 36f, Size.Pixels)
            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .childOf(footer) as KeybindButton

        val addButton = Button("Add Shortcut", Theme.Text.color, fontSize = 13f)
            .setSizing(0f, Size.Fill, 36f, Size.Pixels)
            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                addBinding()
                true
            }
            .childOf(footer)

        addButton.onHover(
            onEnter = { _, _ ->
                val color = if (commandField.value.isEmpty() || keyButton.keys.isEmpty())
                    Theme.Danger.color else Theme.Success.color
                addButton.background.colorTo(color, 300L)
            },
            onExit = { _, _ ->
                addButton.background.colorTo(Theme.Bg.color, 300L)
            }
        )
    }

    private fun renderBindings() {
        listContainer.children.toList().forEach { it.destroy() }
        bindingRows.clear()

        if (KeyShortcuts.bindings.isEmpty()) {
            Text("No keybinds configured...", Theme.TextMuted.color, 14f)
                .setPositioning(0f, Pos.ParentCenter, 40f, Pos.ParentPixels)
                .childOf(listContainer)
            return
        }

        KeyShortcuts.bindings.forEachIndexed { index, binding ->
            createBindingRow(index, binding)
        }
    }

    private fun createBindingRow(index: Int, binding: KeybindEntry) {
        val row = Rectangle(Theme.Bg.color, Theme.Border.color, 6f, 1f)
            .setSizing(100f, Size.ParentPerc, 48f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, if (index == 0) 0f else 8f, Pos.AfterSibling)
            .padding(12f)
            .childOf(listContainer)

        val keyDisplay = Rectangle(Theme.BgLight.color, Theme.BorderMuted.color, 4f, 1f)
            .setSizing(150f, Size.Pixels, 28f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .childOf(row)

        Text(getKeysName(binding.keys), Theme.Text.color, 12f)
            .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
            .childOf(keyDisplay)

        val commandInput = TextInput(binding.command, "Command", hoverColor = null)
            .setSizing(0f, Size.Fill, 28f, Size.Pixels)
            .setPositioning(8f, Pos.AfterSibling, 0f, Pos.ParentCenter)
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

        commandInput.onValueChange { text ->
            KeyShortcuts.updateBinding(index, binding.keys, text as String)
        }

        val deleteButton = Button("×", Theme.Danger.color, fontSize = 18f)
            .setSizing(32f, Size.Pixels, 28f, Size.Pixels)
            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.BgLight.color)
            .borderColor(Theme.BorderMuted.color)
            .borderRadius(4f)
            .hoverColors(null, Theme.Text.color)
            .padding(4f)
            .onClick { _, _, _ ->
                if (KeyShortcuts.removeBinding(index)) renderBindings()
                true
            }
            .childOf(row)

        deleteButton.onHover(
            onEnter = { _, _ -> deleteButton.background.colorTo(Theme.Danger.color, 150L) },
            onExit = { _, _ -> deleteButton.background.colorTo(Theme.BgLight.color, 150L) }
        )

        bindingRows[index] = BindingRow(row, keyDisplay, commandInput, deleteButton)
    }

    private fun addBinding() {
        val command = commandField.value.trim()
        val keys = keyButton.keys.toList()

        if (command.isEmpty() || keys.isEmpty()) return

        if (KeyShortcuts.addBinding(keys, command)) {
            commandField.value = ""
            keyButton.keys.clear()
            keyButton.updateKeyText()
            renderBindings()
        }
    }

    private fun getKeysName(keyList: List<Int>): String {
        if (keyList.isEmpty()) return "None"
        return keyList.joinToString(" + ") { getKeyName(it) }
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
                onComplete = { isAnimating = false }
            )
        }
    }

    inner class KeybindButton : Rectangle(
        Theme.Bg.color,
        Theme.Border.color,
        6f,
        1f,
        floatArrayOf(8f, 8f, 8f, 8f),
        null,
        Theme.BgLight.color
    ) {
        var keys: MutableList<Int> = mutableListOf()
        var listening = false
        private val recordedKeys = mutableSetOf<Int>()

        private val keyText = Text(getKeysName(keys), Theme.Text.color, 12f)
            .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
            .childOf(this)

        init {
            onClick { _, _, button ->
                if (!listening) {
                    startListening()
                } else {
                    val mouseCode = -(button + 1)
                    recordedKeys.add(mouseCode)
                    updateKeyText()
                }
                true
            }

            onRelease { _, _, _ ->
                if (listening && recordedKeys.isNotEmpty()) stopListening()
                true
            }

            onHover(
                onEnter = { _, _ -> colorTo(Theme.BgLight.color) },
                onExit = { _, _ -> colorTo(Theme.Bg.color) }
            )
        }

        private fun startListening() {
            listening = true
            recordedKeys.clear()
            keyText.text = "Click enter or mouse here to confirm"
            backgroundColor(0xFF283440.toInt())
        }

        private fun stopListening() {
            listening = false
            keys = recordedKeys.toMutableList()
            updateKeyText()
            backgroundColor(Theme.Bg.color)
        }

        fun handleKeyPress(keyCode: Int) {
            if (listening && keyCode > 0) {
                recordedKeys.add(keyCode)
                updateKeyText()
            }
        }

        fun handleKeyRelease(keyCode: Int) {
            if (KnitKeys.KEY_ENTER.isPressed && listening && recordedKeys.isNotEmpty() && keyCode > 0) stopListening()
        }

        fun updateKeyText() {
            keyText.text = if (listening) getKeysName(recordedKeys.toList()) else getKeysName(keys)
        }
    }

    override fun onKeyType(typedChar: Char, keyCode: Int, scanCode: Int): Boolean {
        if (keyCode > 0) keyButton.handleKeyPress(keyCode)
        if (keyButton.listening) return true

        val handled = window.charType(keyCode, scanCode, typedChar)
        if (!handled && keyCode == KnitKeys.KEY_ESCAPE.code) {
            onClose()
            return true
        }

        return handled
    }

    //#if MC >= 1.21.9
    //$$ override fun keyPressed(input: net.minecraft.client.input.KeyEvent?): Boolean {
    //$$     val handled = super.keyPressed(input)
    //$$     val input = input?.key ?: return handled
    //$$     if (input > 0) keyButton.handleKeyRelease(input)
    //$$     return handled
    //$$ }
    //#else
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode > 0) keyButton.handleKeyRelease(keyCode)
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
    //#endif
}

