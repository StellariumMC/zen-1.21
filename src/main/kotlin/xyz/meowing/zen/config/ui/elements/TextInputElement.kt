package xyz.meowing.zen.config.ui.elements

import xyz.meowing.vexel.elements.TextInput
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.ui.Theme

class TextInputElement(
    name: String,
    initialValue: String,
    private val placeholder: String
) : VexelElement<TextInputElement>() {
    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 6f, Pos.ParentPixels)
        .childOf(this)

    val input = TextInput(
        initialValue = initialValue,
        placeholder = placeholder,
        backgroundColor = Theme.BgLight.color,
        borderColor = Theme.Border.color
    )
        .setSizing(230f, Size.Pixels, 0f, Size.Auto)
        .setPositioning(10f, Pos.ParentCenter, 28f, Pos.ParentPixels)
        .childOf(this)

    init {
        setSizing(240f, Size.Pixels, 58f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}