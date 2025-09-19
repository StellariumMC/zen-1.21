package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.utils.rendering.NVGRenderer
import meowing.zen.utils.rendering.Font

class Button(
    var text: String = "",
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var hoverTextColor: Int? = null,
    var pressedTextColor: Int? = null,
    fontSize: Float = 12f,
    font: Font = NVGRenderer.defaultFont,
    shadowEnabled: Boolean = false,
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(8f, 16f, 8f, 16f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, widthType, heightType) {

    val innerText = Text(text, textColor, fontSize, shadowEnabled, font)
        .childOf(this)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)

    init {
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        super.onRender(mouseX, mouseY)

        if (text.isNotEmpty()) {
            val currentTextColor = when {
                pressed && pressedTextColor != null -> pressedTextColor!!
                hovered && hoverTextColor != null -> hoverTextColor!!
                else -> textColor
            }

            textColor(currentTextColor)
        }
    }

    fun text(text: String): Button = apply {
        innerText.text = text
    }

    fun textColor(color: Int): Button = apply {
        innerText.textColor = color
    }

    fun fontSize(size: Float): Button = apply {
        innerText.fontSize = size
    }

    fun font(font: Font): Button = apply {
        innerText.font = font
    }

    fun hoverColors(bg: Int? = null, text: Int? = null): Button = apply {
        this.hoverColor = bg
        this.hoverTextColor = text
    }

    fun pressedColors(bg: Int? = null, text: Int? = null): Button = apply {
        this.pressedColor = bg
        this.pressedTextColor = text
    }

    fun shadow(enabled: Boolean = true): Button = apply {
        innerText.shadowEnabled = enabled
    }

    override fun padding(top: Float, right: Float, bottom: Float, left: Float): Button = apply {
        super.padding(top, right, bottom, left)
    }

    override fun padding(all: Float): Button = apply {
        super.padding(all)
    }

    override fun backgroundColor(color: Int): Button = apply {
        super.backgroundColor(color)
    }

    override fun borderColor(color: Int): Button = apply {
        super.borderColor(color)
    }

    override fun borderRadius(radius: Float): Button = apply {
        super.borderRadius(radius)
    }

    override fun borderThickness(thickness: Float): Button = apply {
        super.borderThickness(thickness)
    }

    override fun hoverColor(color: Int): Button = apply {
        super.hoverColor(color)
    }

    override fun pressedColor(color: Int): Button = apply {
        super.pressedColor(color)
    }

    override fun width(newWidth: Float): Button = apply {
        super.width(newWidth)
    }

    override fun height(newHeight: Float): Button = apply {
        super.height(newHeight)
    }
}