package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Size
import meowing.zen.utils.rendering.NVGRenderer

open class Rectangle(
    var backgroundColor: Int = 0x80000000.toInt(),
    var borderColor: Int = 0xFFFFFFFF.toInt(),
    var borderRadius: Float = 0f,
    var borderThickness: Float = 0f,
    var padding: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
    var hoverColor: Int? = null,
    var pressedColor: Int? = null,
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto,
) : CanvasElement<Rectangle>(widthType, heightType) {

    override fun onRender(mouseX: Float, mouseY: Float) {
        val currentBgColor = when {
            pressed && pressedColor != null -> pressedColor!!
            hovered && hoverColor != null -> hoverColor!!
            else -> backgroundColor
        }

        if (currentBgColor != 0) {
            NVGRenderer.rect(x, y, width, height, currentBgColor, borderRadius)
        }

        if (borderThickness > 0f) {
            NVGRenderer.hollowRect(x, y, width, height, borderThickness, borderColor, borderRadius)
        }
    }

    override fun getAutoWidth(): Float {
        val visibleChildren = children.filter { it.visible }
        if (visibleChildren.isEmpty()) return padding[1] + padding[3]

        val minX = visibleChildren.minOf { it.x }
        val maxX = visibleChildren.maxOf { it.x + it.width }

        return (maxX - minX) + padding[3] + padding[1]
    }

    override fun getAutoHeight(): Float {
        val visibleChildren = children.filter { it.visible }
        if (visibleChildren.isEmpty()) return padding[0] + padding[2]

        // Find topmost and bottommost edges
        val minY = visibleChildren.minOf { it.y }
        val maxY = visibleChildren.maxOf { it.y + it.height }

        return (maxY - minY) + padding[0] + padding[2]
    }

    override fun renderChildren(mouseX: Float, mouseY: Float) {
        children.forEach { child ->
            val oldX = child.xConstraint
            val oldY = child.yConstraint
            try {
                child.xConstraint += padding[3]
                child.yConstraint += padding[0]
                child.render(mouseX, mouseY)
            } finally {
                child.xConstraint = oldX
                child.yConstraint = oldY
            }
        }
    }

    open fun padding(top: Float = 0f, right: Float = 0f, bottom: Float = 0f, left: Float = 0f): Rectangle = apply {
        padding[0] = top
        padding[1] = right
        padding[2] = bottom
        padding[3] = left
    }

    open fun padding(all: Float): Rectangle = padding(all, all, all, all)

    open fun backgroundColor(color: Int): Rectangle = apply {
        backgroundColor = color
    }

    open fun borderColor(color: Int): Rectangle = apply {
        borderColor = color
    }

    open fun borderRadius(radius: Float): Rectangle = apply {
        borderRadius = radius
    }

    open fun borderThickness(thickness: Float): Rectangle = apply {
        borderThickness = thickness
    }

    open fun hoverColor(color: Int): Rectangle = apply {
        hoverColor = color
    }

    open fun pressedColor(color: Int): Rectangle = apply {
        pressedColor = color
    }

    open fun width(newWidth: Float): Rectangle = apply {
        width = newWidth
    }

    open fun height(newHeight: Float): Rectangle = apply {
        height = newHeight
    }
}