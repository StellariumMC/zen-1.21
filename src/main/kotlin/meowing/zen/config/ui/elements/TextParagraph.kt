package meowing.zen.config.ui.elements

import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import java.awt.Color

class TextParagraph(
    private val text: String,
    private val centered: Boolean = true,
    private val textColor: Color = Color(100, 245, 255, 255)
) : UIContainer() {
    private lateinit var textComponent: UIWrappedText

    init {
        createComponent()
    }

    private fun createComponent() {
        textComponent = (UIWrappedText(text, centered = centered).constrain {
            x = 2.percent()
            y = CenterConstraint()
            width = 96.percent()
            textScale = 1.0.pixels()
        }.setColor(textColor) childOf this) as UIWrappedText
    }
}