package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.utils.rendering.NVGRenderer
import java.awt.Color
import java.util.UUID

class SvgImage(
    var svgPath: String = "",
    var widthOverride: Float = 80f,
    var heightOverride: Float = 80f,
    var color: Color = Color.WHITE
) : CanvasElement<SvgImage>() {
    var imageId = "${UUID.randomUUID()}"
    var image = NVGRenderer.createImage(svgPath, widthOverride.toInt(), heightOverride.toInt(), color, imageId)

    init {
        width = widthOverride
        height = heightOverride
        setSizing(Size.Auto, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreMouseEvents()
        setSizing(widthOverride, Size.Pixels, heightOverride, Size.Pixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (svgPath.isEmpty()) return

        widthOverride = width
        heightOverride = height

        NVGRenderer.svg(imageId, x, y, widthOverride, heightOverride, color.alpha / 255f) // Draw the SVG with a red tint
    }

    fun setSvgColor(newColor: Color) {
        if (color != newColor) {
            color = newColor
            reloadImage()
        }
    }

    private fun reloadImage() {
        image = NVGRenderer.createImage(svgPath, widthOverride.toInt(), heightOverride.toInt(), color, imageId)
    }
}