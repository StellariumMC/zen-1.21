package meowing.zen.canvas.core.elements

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.fadeIn
import meowing.zen.canvas.core.animations.fadeOut
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Text

class Dropdown(
    var options: List<String>,
    var selectedIndex: Int = 0,
    val backgroundColor: Int = 0xFF171616.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 3f,
    padding: FloatArray = floatArrayOf(4f, 4f, 4f, 4f),
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
        .childOf(this)

    private val selectedText = Text(options[selectedIndex], 0xFFFFFFFF.toInt(), fontSize)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
        .childOf(previewRect)

    private var pickerPanel: DropDownPanel? = null

    init {
        setSizing(140f, Size.Pixels, 0f, Size.Auto)
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
            .setPositioning(previewRect.getScreenX(), Pos.ScreenPixels, previewRect.getScreenY(), Pos.ScreenPixels)
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
    backgroundColor: Int = 0xFF171616.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    selectedColor: Int = 0xFF303030.toInt(),
    fontSize: Float = 12f,
) : CanvasElement<DropDownPanel>() {
    val backgroundPopup = Rectangle(backgroundColor, borderColor, 2f, 3f, floatArrayOf(4f, 4f, 4f, 4f))
        .setSizing(width, widthType, 120f,Size.Pixels)
        .scrollable(true)
        .childOf(this)

    init {
        setSizing(0f, Size.Auto, 0f,Size.Auto)
        setFloating()

        val rect = Rectangle(selectedColor, borderColor, 2f, 1f, floatArrayOf(2f, 2f, 2f, 2f))
            .setSizing(100f, Size.ParentPerc, 0f,Size.Auto)
            .setPositioning(0f, Pos.ParentPixels, 1f, Pos.AfterSibling)
            .childOf(backgroundPopup)

        Text(options[selectedIndex], 0xFFFFFFFF.toInt(), fontSize)
            .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
            .childOf(rect)

        options.forEachIndexed { index, option ->
            if(index == selectedIndex) return@forEachIndexed

            val rect = Rectangle(backgroundColor, borderColor, 2f, 1f, floatArrayOf(2f, 2f, 2f, 2f), hoverColor = 0x80505050.toInt())
                .setSizing(100f, Size.ParentPerc, 0f,Size.Auto)
                .setPositioning(0f, Pos.ParentPixels, 1f, Pos.AfterSibling)
                .onClick { _, _, _ ->
                    onValueChange?.invoke(index)
                    true
                }
                .childOf(backgroundPopup)

            Text(option, 0xFFFFFFFF.toInt(), fontSize)
                .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
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