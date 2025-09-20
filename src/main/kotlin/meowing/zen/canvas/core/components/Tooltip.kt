package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.fadeIn
import meowing.zen.canvas.core.animations.fadeOut
import java.awt.Color

class Tooltip(
    backgroundColor: Int = 0xFF1e1e1e.toInt(),
    borderColor: Int = 0xFF555759.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(4f, 4f, 4f, 4f),
    hoverColor: Int? = 0xFF1e1e1e.toInt(),
    pressedColor: Int? = 0xFF1e1e1e.toInt(),
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, widthType, heightType) {
    val innerText = Text("Tooltip", 0xFFFFFFFF.toInt(), 12f)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(this)

    init {
        innerText.visible = false
        setSizing(Size.Auto, Size.Auto)
        setPositioning(0f, Pos.ParentCenter, -40f, Pos.ParentPixels)
        ignoreMouseEvents()
        setFloating()
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
         if( !visible) return
        super.onRender(mouseX, mouseY)
    }
}