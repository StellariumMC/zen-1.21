package meowing.zen.canvas.core.elements

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.fadeIn
import meowing.zen.canvas.core.animations.fadeOut
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.SvgImage
import meowing.zen.canvas.core.components.Text
import java.awt.Color

class Dropdown(
    var options: List<String>,
    var selectedIndex: Int = 0,
    val backgroundColor: Int = 0xFF282e3a.toInt(),
    var iconColor: Int = 0xFF4c87f9.toInt(),
    borderColor: Int = 0xFF0194d8.toInt(),
    borderRadius: Float = 6f,
    borderThickness: Float = 2f,
    padding: FloatArray = floatArrayOf(6f, 6f, 6f, 6f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : CanvasElement<Dropdown>(widthType, heightType) {
    var fontSize = 12f
    private var isPickerOpen = false
    private var isAnimating = false

    private val previewRect = Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, Size.ParentPerc, Size.ParentPerc)
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF062897.toInt())
        .childOf(this)

    private val selectedText = Text(options[selectedIndex], 0xFFFFFFFF.toInt(), fontSize)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .childOf(previewRect)

    val dropdownArrow = SvgImage(svgPath = "/assets/zen/dropdown.svg", color = Color(iconColor))
        .setSizing(20f, Size.Pixels, 20f, Size.Pixels)
        .setPositioning(80f, Pos.ParentPercent, 0f, Pos.ParentCenter)
        .childOf(previewRect)

    private var pickerPanel: DropDownPanel? = null

    init {
        setSizing(180f, Size.Pixels, 0f, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)

        onClick { _, _, _ ->
            if (!isAnimating) togglePicker()
            true
        }
    }

    private fun togglePicker() {
        if (isPickerOpen) closePicker() else openPicker()
    }

    private fun openPicker() {
        if (isPickerOpen || isAnimating) return
        isAnimating = true

        pickerPanel = DropDownPanel(selectedIndex, options, fontSize = fontSize)
            .setSizing(previewRect.width, Size.Pixels, 0f, Size.Auto)
            .setPositioning(previewRect.getScreenX(), Pos.ScreenPixels, previewRect.getScreenY() + previewRect.height + 4f, Pos.ScreenPixels)
            .childOf(getRootElement())

        pickerPanel?.onValueChange { index ->
            selectedIndex = index as Int
            selectedText.text = options[selectedIndex]
            closePicker()
            onValueChange?.invoke(index)
        }

        pickerPanel?.backgroundPopup?.fadeIn(200, EasingType.EASE_OUT) {
            isAnimating = false
        }

        isPickerOpen = true
    }

    override fun getAutoWidth(): Float {
        return previewRect.getAutoWidth()
    }
    override fun getAutoHeight(): Float {
        return previewRect.getAutoHeight()
    }

    private fun closePicker() {
        if (!isPickerOpen || pickerPanel == null || isAnimating) return
        isAnimating = true

        pickerPanel?.backgroundPopup?.fadeOut(200, EasingType.EASE_IN) {
            getRootElement().children.remove(pickerPanel!!)
            pickerPanel!!.destroy()
            pickerPanel = null
            isAnimating = false
        }

        isPickerOpen = false
    }

    override fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        val handled = super.handleMouseClick(mouseX, mouseY, button)

        if (isPickerOpen && pickerPanel != null && !pickerPanel!!.isPointInside(mouseX, mouseY) && !isPointInside(mouseX, mouseY) && !isAnimating) {
            closePicker()
        }

        return handled
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        previewRect.isHovered = hovered
        previewRect.isPressed = pressed
    }

    override fun destroy() {
        if (isPickerOpen) closePicker()
        super.destroy()
    }

    fun fontSize(size: Float): Dropdown = apply {
        fontSize = size
        selectedText.fontSize = size
    }
}

class DropDownPanel(
    selectedIndex: Int,
    options: List<String> = listOf(),
    backgroundColor: Int = 0xFF333741.toInt(),
    borderColor: Int = 0xFF3e414b.toInt(),
    selectedColor: Int = 0xFF2a2f35.toInt(),
    fontSize: Float = 12f,
) : CanvasElement<DropDownPanel>() {
    val backgroundPopup = Rectangle(backgroundColor, borderColor, 8f, 1f, floatArrayOf(7f, 7f, 7f, 7f))
        .setSizing(width, widthType, 120f,Size.Pixels)
        .scrollable(true)
        .childOf(this)
        .dropShadow()

    init {
        setSizing(0f, Size.Auto, 0f,Size.Auto)
        setFloating()

        options.forEachIndexed { index, option ->
            val rect = Rectangle(if(index == selectedIndex) selectedColor else backgroundColor, borderColor, 5f, 0f, floatArrayOf(5f, 5f, 5f, 5f), hoverColor = 0x80505050.toInt())
                .setSizing(100f, Size.ParentPerc, 0f,Size.Auto)
                .setPositioning(0f, Pos.ParentPixels, 1f, Pos.AfterSibling)
                .onClick { _, _, _ ->
                    onValueChange?.invoke(index)
                    true
                }
                .childOf(backgroundPopup)

            Text(option, 0xFFFFFFFF.toInt(), fontSize)
                .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
                .childOf(rect)
                .ignoreMouseEvents()
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        backgroundPopup.setSizing(width, widthType, 170f,Size.Pixels)
    }

    override fun getAutoHeight(): Float {
        return backgroundPopup.height
    }

}