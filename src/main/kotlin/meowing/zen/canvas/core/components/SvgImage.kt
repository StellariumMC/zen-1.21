package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.animateFloat
import meowing.zen.utils.rendering.NVGRenderer
import java.awt.Color
import java.util.UUID

class SvgImage(
    var svgPath: String = "",
    var startingWidth: Float = 80f,
    var startingHeight: Float = 80f,
    var color: Color = Color.WHITE
) : CanvasElement<SvgImage>() {
    var imageId = "${UUID.randomUUID()}"
    var image = NVGRenderer.createImage(svgPath, startingWidth.toInt(), startingHeight.toInt(), color, imageId)
    var rotation: Float = 0f

    init {
        width = startingWidth
        height = startingHeight
        setSizing(Size.Auto, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreMouseEvents()
        setSizing(startingWidth, Size.Pixels, startingHeight, Size.Pixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (svgPath.isEmpty()) return

        startingWidth = width
        startingHeight = height

        val centerX = x + width / 2f
        val centerY = y + height / 2f

        if (rotation != 0f) {
            NVGRenderer.push()
            NVGRenderer.translate(centerX, centerY)
            NVGRenderer.rotate(Math.toRadians(rotation.toDouble()).toFloat())
            NVGRenderer.translate(-centerX, -centerY)
        }

        NVGRenderer.svg(imageId, x, y, startingWidth, startingHeight, color.alpha / 255f)

        if (rotation != 0f) {
            NVGRenderer.pop()
        }
    }

    fun rotateTo(angle: Float, duration: Long = 300, type: EasingType = EasingType.EASE_OUT, onComplete: (() -> Unit)? = null): SvgImage {
        animateFloat({ rotation }, { rotation = it }, angle, duration, type, onComplete = onComplete)
        return this
    }

    fun setSvgColor(newColor: Color) {
        if (color != newColor) {
            color = newColor
            reloadImage()
        }
    }

    private fun reloadImage() {
        image = NVGRenderer.createImage(svgPath, startingWidth.toInt(), startingHeight.toInt(), color, imageId)
    }
}