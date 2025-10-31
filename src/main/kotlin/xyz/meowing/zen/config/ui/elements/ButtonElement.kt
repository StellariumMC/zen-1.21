package xyz.meowing.zen.config.ui.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.ui.Theme

class ButtonElement(
    text: String,
    private val onClick: () -> Unit
) : VexelElement<ButtonElement>() {

    private val button = Rectangle(Theme.Bg.color, Theme.Border.color, 5f, 1f, floatArrayOf(8f, 16f, 8f, 16f))
        .setSizing(228f, Size.Pixels, 0f, Size.Auto)
        .setPositioning(6f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .childOf(this)

    private val buttonText = Text(text, Theme.Text.color, 16f)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(button)

    init {
        setSizing(240f, Size.Pixels, 40f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)

        button.onHover(
            { _, _ -> button.colorTo(Theme.BgLight.color, 150, EasingType.EASE_OUT) },
            { _, _ -> button.colorTo(Theme.Bg.color, 150, EasingType.EASE_IN) }
        )

        button.onClick { _, _, btn ->
            if (btn == 0) {
                button.colorTo(Theme.Highlight.withAlpha(0.8f), 100, EasingType.EASE_OUT) {
                    button.colorTo(Theme.BgLight.color, 100, EasingType.EASE_IN)
                }
                onClick()
                true
            } else false
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}