package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import meowing.zen.config.ui.core.ConfigTheme
import meowing.zen.utils.Utils.createBlock
import org.lwjgl.glfw.GLFW
import java.awt.Color

class KeybindElement(
    private var code: Int = 0,
    private val onKeyChange: ((Int) -> Unit)? = null,
    private val theme: ConfigTheme = ConfigTheme()
) : UIContainer() {
    private var listening = false
    private var keyDisplay: UIText
    private val border: UIComponent
    private val container: UIComponent

    init {
        border = createBlock(3f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(Color(18, 22, 26, 0)) childOf this

        container = createBlock(3f).constrain {
            x = 1.pixels()
            y = 1.pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(Color(18, 22, 26, 255)) childOf border

        keyDisplay = (UIText(getKeyName(code)).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        }.setColor(Color.WHITE) childOf container) as UIText

        setupEventHandlers()
    }

    private fun setupEventHandlers() {
        onMouseEnter {
            if (!listening) {
                border.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.3f, Color(170, 230, 240, 127).toConstraint())
                }
                container.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.3f, Color(28, 32, 36, 255).toConstraint())
                }
            }
        }

        onMouseLeave {
            if (!listening) {
                border.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.3f, Color(18, 22, 26, 0).toConstraint())
                }
                container.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.3f, Color(18, 22, 26, 255).toConstraint())
                }
            }
        }

        container.onMouseClick {
            grabWindowFocus()
            listening = true
            keyDisplay.setText(".....")
            border.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(170, 230, 240, 255).toConstraint())
            }
            container.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.brighter().toConstraint())
            }
        }

        container.onKeyType { _, keycode ->
            if (listening) {
                if (keycode == 256) {
                    keyDisplay.setText("None").setColor(Color.WHITE)
                    code = 0
                    onKeyChange?.invoke(0)
                } else {
                    keyDisplay.setText(getKeyName(keycode)).setColor(Color.WHITE)
                    code = keycode
                    onKeyChange?.invoke(keycode)
                }
                listening = false
                border.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(18, 22, 26, 0).toConstraint())
                }
                container.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.toConstraint())
                }
                loseFocus()
            }
        }
    }

    override fun keyType(typedChar: Char, keyCode: Int) {
        if (keyCode == 256 && listening) {
            keyDisplay.setText("None").setColor(Color.WHITE)
            code = 0
            onKeyChange?.invoke(0)
            listening = false
            border.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(18, 22, 26, 0).toConstraint())
            }
            container.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.toConstraint())
            }
            loseFocus()
            return
        }
        super.keyType(typedChar, keyCode)
    }

    private fun getKeyName(keyCode: Int): String = when (keyCode) {
        340 -> "LShift"
        344 -> "RShift"
        341 -> "LCtrl"
        345 -> "RCtrl"
        342 -> "LAlt"
        346 -> "RAlt"
        257 -> "Enter"
        256 -> "None"
        in 290..301 -> "F${keyCode - 289}"
        else -> GLFW.glfwGetKeyName(keyCode, 0)?.uppercase() ?: "Key $keyCode"
    }
}