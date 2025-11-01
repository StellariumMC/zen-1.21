package xyz.meowing.zen.config.ui.elements

import xyz.meowing.knit.api.input.KnitMouseButtons
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.elements.Slider
import xyz.meowing.zen.ui.Theme

class SliderElement(
    name: String,
    initialValue: Double,
    private val min: Double,
    private val max: Double,
    private val showDouble: Boolean
) : VexelElement<SliderElement>() {

    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 8f, Pos.ParentPixels)
        .childOf(this)

    private val valueText = Text(formatValue(initialValue), Theme.TextMuted.color, 16f)
        .setPositioning(-6f, Pos.ParentPixels, 8f, Pos.ParentPixels)
        .alignRight()
        .childOf(this)

    val slider = Slider(
        value = ((initialValue - min) / (max - min)).toFloat(),
        minValue = 0f,
        maxValue = 1f,
        thumbColor = 0xFFFFFFFF.toInt(),
        trackColor = Theme.Bg.color,
        trackFillColor = Theme.Primary.color
    )
        .setSizing(220f, Size.Pixels, 24f, Size.Pixels)
        .setPositioning(10f, Pos.ParentPixels, 0f, Pos.ParentPixels)
        .alignBottom()
        .setOffset(0f, -8f)
        .childOf(this)

    init {
        setSizing(240f, Size.Pixels, 48f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)

        slider.onValueChange { sliderValue ->
            val actualValue = min + (sliderValue as Float) * (max - min)
            valueText.text = formatValue(actualValue)
        }
    }

    private fun formatValue(value: Double): String =
        if (showDouble) String.format("%.1f", value)
        else value.toInt().toString()

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (slider.isDragging && !KnitMouseButtons.LEFT.isPressed) {
            slider.isDragging = false
        }
    }
}