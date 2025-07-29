package meowing.zen.config.ui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import meowing.zen.utils.Utils.createBlock
import java.awt.Color

class Button(
    text: String,
    private val onClick: (() -> Unit)? = null
) : UIContainer() {
    private val normalBg = Color(15, 20, 25, 255)
    private val pressedBg = Color(40, 80, 90, 255)
    private val textColor = Color(100, 245, 255, 255)

    init {
        val container = createBlock(6f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(normalBg) childOf this

        val buttonComponent = createBlock(6f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(normalBg) childOf container

        UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.1.pixels()
        }.setColor(textColor) childOf buttonComponent

        onMouseClick {
            onClick?.invoke()
            buttonComponent.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, pressedBg.toConstraint())
                onComplete { setColor(normalBg) }
            }
        }
    }
}