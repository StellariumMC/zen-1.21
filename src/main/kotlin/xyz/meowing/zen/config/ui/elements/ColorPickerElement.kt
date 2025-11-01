package xyz.meowing.zen.config.ui.elements

import xyz.meowing.knit.api.input.KnitMouseButtons
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateSize
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer
import xyz.meowing.vexel.utils.style.Gradient
import xyz.meowing.zen.ui.Theme
import xyz.meowing.zen.config.ui.panels.SectionButton
import java.awt.Color
import kotlin.math.roundToInt

class ColorPickerElement(
    name: String,
    initialColor: Color
) : VexelElement<ColorPickerElement>() {
    var selectedColor: Color = initialColor
    private var expanded = false
    private var isAnimating = false

    var currentHue: Float
    var currentSaturation: Float
    var currentBrightness: Float
    var currentAlpha = initialColor.alpha / 255f
    var draggingPicker = false
    var draggingHue = false
    var draggingAlpha = false

    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 8f, Pos.ParentPixels)
        .childOf(this)

    private val previewRect = Rectangle(
        selectedColor.rgb,
        Theme.Border.color,
        3f,
        1f,
        hoverColor = selectedColor.darker().rgb
    )
        .setSizing(30f, Size.Pixels, 20f, Size.Pixels)
        .setPositioning(-6f, Pos.ParentPixels, 0f, Pos.MatchSibling)
        .alignRight()
        .setOffset(0f, -2f)
        .childOf(this)

    private val pickerContainer = Rectangle(
        0x00000000,
        0x00000000,
        0f,
        0f
    )
        .setSizing(228f, Size.Pixels, 0f, Size.Pixels)
        .setPositioning(6f, Pos.ParentPixels, 34f, Pos.ParentPixels)
        .childOf(this)

    private val pickerArea = ColorPickerArea()
        .setSizing(228f, Size.Pixels, 140f, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
        .childOf(pickerContainer)

    private val hueSlider = HueSlider()
        .setSizing(228f, Size.Pixels, 15f, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
        .childOf(pickerContainer)

    private val alphaSlider = AlphaSlider()
        .setSizing(228f, Size.Pixels, 15f, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
        .childOf(pickerContainer)

    init {
        setSizing(240f, Size.Pixels, 32f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)

        val hsb = Color.RGBtoHSB(initialColor.red, initialColor.green, initialColor.blue, null)
        currentHue = hsb[0]
        currentSaturation = hsb[1]
        currentBrightness = hsb[2]
        updateColor()

        previewRect.onClick { _, _, _ ->
            if (!isAnimating) toggleExpanded()
            true
        }

        setupInteractions()
        pickerContainer.visible = false
    }

    private fun setupInteractions() {
        pickerArea.onMouseClick { mouseX, mouseY, _ ->
            draggingPicker = true
            updatePickerFromMouse(mouseX, mouseY)
            true
        }

        hueSlider.onMouseClick { mouseX, _, _ ->
            draggingHue = true
            updateHueFromMouse(mouseX)
            true
        }

        alphaSlider.onMouseClick { mouseX, _, _ ->
            draggingAlpha = true
            updateAlphaFromMouse(mouseX)
            true
        }
    }

    override fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        val result = super.handleMouseMove(mouseX, mouseY)

        when {
            draggingPicker -> updatePickerFromMouse(mouseX, mouseY)
            draggingHue -> updateHueFromMouse(mouseX)
            draggingAlpha -> updateAlphaFromMouse(mouseX)
        }

        return result
    }

    override fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        val result = super.handleMouseRelease(mouseX, mouseY, button)

        if (button == 0) {
            draggingPicker = false
            draggingHue = false
            draggingAlpha = false
        }

        return result
    }

    private fun updatePickerFromMouse(mouseX: Float, mouseY: Float) {
        val relativeX = (mouseX - pickerArea.x) / pickerArea.width
        val relativeY = (mouseY - pickerArea.y) / pickerArea.height

        currentSaturation = relativeX.coerceIn(0f, 1f)
        currentBrightness = (1f - relativeY).coerceIn(0f, 1f)

        updateColor()
    }

    private fun updateHueFromMouse(mouseX: Float) {
        val relativeX = (mouseX - hueSlider.x) / hueSlider.width
        currentHue = relativeX.coerceIn(0f, 1f)
        updateColor()
    }

    private fun updateAlphaFromMouse(mouseX: Float) {
        val relativeX = (mouseX - alphaSlider.x) / alphaSlider.width
        currentAlpha = relativeX.coerceIn(0f, 1f)
        updateColor()
    }

    private fun updateColor() {
        val rgb = Color.HSBtoRGB(currentHue, currentSaturation, currentBrightness)
        val baseColor = Color(rgb)
        val alpha = (currentAlpha * 255).roundToInt().coerceIn(0, 255)

        selectedColor = Color(baseColor.red, baseColor.green, baseColor.blue, alpha)
        previewRect.backgroundColor = selectedColor.rgb

        pickerArea.currentHue = currentHue
        alphaSlider.currentColor = Color(baseColor.red, baseColor.green, baseColor.blue)

        onValueChange.forEach { it.invoke(selectedColor) }
    }

    private fun toggleExpanded() {
        expanded = !expanded
        isAnimating = true
        if (expanded) {
            pickerContainer.visible = true
            val targetHeight = 32f + 190f
            animateSize(240f, targetHeight, 200, EasingType.EASE_OUT) {
                isAnimating = false
                invalidateParentLayout()
            }
            pickerContainer.animateSize(228f, 175f, 200, EasingType.EASE_OUT)
        } else {
            animateSize(240f, 32f, 200, EasingType.EASE_IN) {
                pickerContainer.visible = false
                isAnimating = false
                invalidateParentLayout()
            }
            pickerContainer.animateSize(228f, 0f, 200, EasingType.EASE_IN)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun invalidateParentLayout() {
        var current = parent as? VexelElement<SectionButton>

        while (current != null && current !is SectionButton) {
            current = current.parent as? VexelElement<SectionButton>
        }

        current?.recalculateHeight()
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (draggingPicker && !KnitMouseButtons.LEFT.isPressed) draggingPicker = false
        if (draggingAlpha && !KnitMouseButtons.LEFT.isPressed) draggingAlpha = false
        if (draggingHue && !KnitMouseButtons.LEFT.isPressed) draggingHue = false
    }

    inner class ColorPickerArea : VexelElement<ColorPickerArea>() {
        var currentHue = 0f

        override fun onRender(mouseX: Float, mouseY: Float) {
            val hueColor = Color.HSBtoRGB(currentHue, 1f, 1f)
            val whiteColor = 0xFFFFFFFF.toInt()
            val blackColor = 0xFF000000.toInt()

            NVGRenderer.gradientRect(x, y, width, height, whiteColor, hueColor, Gradient.LeftToRight, 0f)
            NVGRenderer.gradientRect(x, y, width, height + 1f, 0x00000000, blackColor, Gradient.TopToBottom, 0f)

            val indicatorX = x + currentSaturation * width
            val indicatorY = y + (1f - currentBrightness) * height
            NVGRenderer.hollowRect(indicatorX - 3f, indicatorY - 3f, 6f, 6f, 2f, 0xFFFFFFFF.toInt(), 2f)
        }
    }

    inner class HueSlider : VexelElement<HueSlider>() {
        override fun onRender(mouseX: Float, mouseY: Float) {
            val steps = (width / 1f).toInt()
            val stepWidth = width / steps

            for (i in 0 until steps) {
                val hue = i.toFloat() / steps
                val rgb = Color.HSBtoRGB(hue, 1f, 1f)
                val color = Color(rgb)

                val rectX = x + i * stepWidth
                NVGRenderer.rect(rectX, y, stepWidth, height, color.rgb, 0f)
            }

            val indicatorX = x + currentHue * width
            NVGRenderer.rect(indicatorX - 2f, y - 2f, 4f, height + 4f, 0xFFFFFFFF.toInt(), 3f)
        }
    }

    inner class AlphaSlider : VexelElement<AlphaSlider>() {
        var currentColor: Color = Color.WHITE

        override fun onRender(mouseX: Float, mouseY: Float) {
            val opaqueColor = Color(currentColor.red, currentColor.green, currentColor.blue, 255).rgb
            val transparentColor = Color(currentColor.red, currentColor.green, currentColor.blue, 0).rgb

            NVGRenderer.gradientRect(x, y, width, height, transparentColor, opaqueColor, Gradient.LeftToRight, 0f)

            val indicatorX = x + currentAlpha * width
            NVGRenderer.rect(indicatorX - 2f, y - 2f, 4f, height + 4f, 0xFFFFFFFF.toInt(), 2f)
        }
    }
}