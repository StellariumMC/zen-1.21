package xyz.meowing.zen.config.ui.elements

import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.input.KnitInputs
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer
import xyz.meowing.zen.ui.Theme

class KeybindElement(
    name: String,
    initialKeyCode: Int
) : VexelElement<KeybindElement>() {

    var selectedKey: Int = initialKeyCode
        private set
    private var listening = false

    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .ignoreMouseEvents()
        .childOf(this)

    private val keybindButton = Rectangle(Theme.BgLight.color, Theme.Border.color, 5f, 1.5f)
        .setPositioning(-6f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .alignRight()
        .childOf(this)

    private val keybindText = Text(getKeyName(initialKeyCode), Theme.Text.color, 16f)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(keybindButton)

    init {
        setSizing(240f, Size.Pixels, 32f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)
        ignoreFocus()

        updateButtonSize()

        keybindButton.onHover(
            { _, _ ->
                if (!listening) {
                    keybindButton.colorTo(Theme.Highlight.color, 150, EasingType.EASE_OUT)
                }
            },
            { _, _ ->
                if (!listening) {
                    keybindButton.colorTo(Theme.BgLight.color, 150, EasingType.EASE_IN)
                }
            }
        )

        keybindButton.onClick { _, _, button ->
            if (listening) {
                setKey(-100 + button)
                false
            } else if (button == 0) {
                startListening()
                true
            } else {
                false
            }
        }

        onCharType { keyCode, _, _ ->
            if (!listening) return@onCharType false
            when (keyCode) {
                GLFW.GLFW_KEY_ESCAPE -> {
                    stopListening()
                    true
                }
                GLFW.GLFW_KEY_BACKSPACE -> {
                    setKey(0)
                    true
                }
                GLFW.GLFW_KEY_ENTER -> {
                    stopListening()
                    true
                }
                else -> {
                    setKey(keyCode)
                    true
                }
            }
        }
    }

    private fun startListening() {
        listening = true
        keybindText.text = "..."
        updateButtonSize()
        keybindButton.colorTo(Theme.Primary.color, 150, EasingType.EASE_OUT)
    }

    private fun stopListening() {
        listening = false
        keybindText.text = getKeyName(selectedKey)
        updateButtonSize()
        keybindButton.colorTo(Theme.BgLight.color, 150, EasingType.EASE_IN)
    }

    private fun setKey(keyCode: Int) {
        selectedKey = keyCode
        onValueChange.forEach { it.invoke(selectedKey) }
        stopListening()
    }

    private fun updateButtonSize() {
        val textWidth = NVGRenderer.textWidth(keybindText.text, 16f, NVGRenderer.defaultFont)
        keybindButton.setSizing(textWidth + 12f, Size.Pixels, 20f, Size.Pixels)
    }

    private fun getKeyName(keyCode: Int): String = when (keyCode) {
        0 -> "None"
        in -100..-1 -> "Mouse ${keyCode + 100}"
        else -> KnitInputs.getDisplayName(keyCode)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}