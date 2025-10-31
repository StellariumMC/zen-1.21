package xyz.meowing.zen.config.ui.elements

import xyz.meowing.vexel.elements.Switch
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.ui.Theme

class SwitchElement(
    name: String,
    initialValue: Boolean
) : VexelElement<SwitchElement>() {
    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .childOf(this)

    val switch = Switch(
        thumbColor = 0xFFFFFFFF.toInt(),
        trackEnabledColor = Theme.Success.color,
        trackDisabledColor = Theme.BgDark.color,
        thumbRadius = 5f,
        borderRadius = 5f,
        thumbWidth = 12f,
        thumbHeight = 12f,
        padding = floatArrayOf(0f, 0f, 0f, 0f)
    )
        .setSizing(40f, Size.Pixels, 16f, Size.Pixels)
        .setPositioning(-5f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .alignRight()
        .childOf(this)

    init {
        setSizing(240f, Size.Pixels, 32f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)
        switch.setEnabled(initialValue, animated = false, silent = true)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}