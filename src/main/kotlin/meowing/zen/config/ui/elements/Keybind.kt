package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import meowing.zen.config.ui.core.ConfigTheme
import meowing.zen.utils.Utils.createBlock
import org.lwjgl.glfw.GLFW
import java.awt.Color

class Keybind(
    private var keyCode: Int = 0,
    private val onKeyChange: ((Int) -> Unit)? = null,
    private val theme: ConfigTheme = ConfigTheme()
) : UIComponent() {

    private var listening = false
    private var keyDisplay: UIText

    init {
        val container = createBlock(6f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(theme.element) childOf this

        keyDisplay = (UIText(getKeyName(keyCode)).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        }.setColor(Color.WHITE) childOf container) as UIText

        container.onMouseClick {
            grabWindowFocus()
            listening = true
            keyDisplay.setText(".....")
            container.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.brighter().toConstraint())
            }
        }

        container.onKeyType { _, keycode ->
            if (listening) {
                keyDisplay.setText(getKeyName(keycode)).setColor(Color.WHITE)
                keyCode = keycode
                onKeyChange?.invoke(keycode)
                listening = false
                container.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.brighter().toConstraint())
                }
                loseFocus()
            }
        }
    }

    private fun getKeyName(keyCode: Int): String = when (keyCode) {
        340 -> "LShift"
        344 -> "RShift"
        341 -> "LCtrl"
        345 -> "RCtrl"
        342 -> "LAlt"
        346 -> "RAlt"
        257 -> "Enter"
        256 -> "Escape"
        in 290..301 -> "F${keyCode - 289}"
        else -> GLFW.glfwGetKeyName(keyCode, 0)?.uppercase() ?: "Key $keyCode"
    }
}