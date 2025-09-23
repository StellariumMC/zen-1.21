package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.animateFloat
import meowing.zen.canvas.core.animations.fadeIn
import meowing.zen.canvas.core.animations.fadeOut
import meowing.zen.utils.rendering.Gradient
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
    var scrollable: Boolean = false
) : CanvasElement<Rectangle>(widthType, heightType) {
    var secondBorderColor: Int = borderColor
    var gradientType: Gradient = Gradient.TopLeftToBottomRight
    var scrollOffset: Float = 0f
    var dropShadow: Boolean = false
    var rotation: Float = 0f

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (!visible || (height - (padding[0] + padding[2])) == 0f || (width - (padding[1] + padding[3])) == 0f) return

        val currentBgColor = when {
            pressed && pressedColor != null -> pressedColor!!
            hovered && hoverColor != null -> hoverColor!!
            else -> backgroundColor
        }

        val centerX = x + width / 2f
        val centerY = y + height / 2f

        if (rotation != 0f) {
            NVGRenderer.push()
            NVGRenderer.translate(centerX, centerY)
            NVGRenderer.rotate(Math.toRadians(rotation.toDouble()).toFloat())
            NVGRenderer.translate(-centerX, -centerY)
        }

        if(dropShadow) {
            NVGRenderer.dropShadow(x, y, width, height, 30f, 1f, borderRadius)
        }

        if (currentBgColor != 0) {
            NVGRenderer.rect(x, y, width, height, currentBgColor, borderRadius)
        }

        if (borderThickness > 0f) {
            if (borderColor != secondBorderColor) {
                NVGRenderer.hollowGradientRect(x, y, width, height, borderThickness, borderColor, secondBorderColor, gradientType, borderRadius)
            } else {
                NVGRenderer.hollowRect(x, y, width, height, borderThickness, borderColor, borderRadius)
            }
        }

        if (rotation != 0f) {
            NVGRenderer.pop()
        }
    }

    override fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        if (!isMouseOnVisible(mouseX, mouseY)) return false

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val childHandled = children.reversed().any { it.handleMouseScroll(mouseX, adjustedMouseY, horizontal, vertical) }

        if (!childHandled && scrollable && isPointInside(mouseX, mouseY)) {
            val contentHeight = getContentHeight()
            val viewHeight = height - padding[0] - padding[2]

            if (contentHeight > viewHeight) {
                val scrollAmount = vertical.toFloat() * -30f
                val maxScroll = contentHeight - viewHeight
                scrollOffset = (scrollOffset + scrollAmount).coerceIn(0f, maxScroll)
                return true
            }
        }

        return childHandled
    }

    override fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> {
                mouseEnterListeners.forEach { it(mouseX, mouseY) }
                tooltipElement?.let {
                    it.fadeIn(200, EasingType.EASE_OUT)
                    it.innerText.fadeIn(200, EasingType.EASE_OUT)
                }
            }
            !isHovered && wasHovered -> {
                mouseExitListeners.forEach { it(mouseX, mouseY) }
                tooltipElement?.let {
                    it.fadeOut(200, EasingType.EASE_OUT)
                    it.innerText.fadeOut(200, EasingType.EASE_OUT)
                }
            }
        }

        if (isHovered) mouseMoveListeners.forEach { it(mouseX, mouseY) }

        val childHandled = if (scrollable && !isMouseOnVisible(mouseX, mouseY)) {
            false
        } else {
            children.reversed().any { it.handleMouseMove(mouseX, adjustedMouseY) }
        }

        return childHandled || isHovered
    }

    override fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        if (scrollable && !isMouseOnVisible(mouseX, mouseY)) return false

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val childHandled = children.reversed().any { it.handleMouseClick(mouseX, adjustedMouseY, button) }

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                mouseClickListeners.any { it(mouseX, mouseY, button) } || mouseClickListeners.isEmpty()
            }
            else -> false
        }
    }

    override fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val wasPressed = isPressed
        isPressed = false

        val childHandled = if (scrollable && !isMouseOnVisible(mouseX, mouseY)) {
            false
        } else {
            children.reversed().any { it.handleMouseRelease(mouseX, adjustedMouseY, button) }
        }

        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) && (mouseReleaseListeners.any { it(mouseX, mouseY, button) } || mouseReleaseListeners.isEmpty()))
    }

    fun getContentHeight(): Float {
        val visibleChildren = children.filter { !it.isFloating }
        if (visibleChildren.isEmpty()) return 0f

        val bottomChild = visibleChildren.maxByOrNull { it.y + it.height } ?: return 0f
        return bottomChild.y + bottomChild.height - (y + padding[0])
    }

    fun isMouseOnVisible(mouseX: Float, mouseY: Float): Boolean {
        if (!scrollable) return true

        val contentX = x + padding[3]
        val contentY = y + padding[0]
        val viewWidth = width - padding[1] - padding[3]
        val viewHeight = height - padding[0] - padding[2]

        return mouseX >= contentX && mouseX <= contentX + viewWidth && mouseY >= contentY && mouseY <= contentY + viewHeight
    }

    public override fun getAutoWidth(): Float {
        val visibleChildren = children.filter { it.visible && !it.isFloating }
        if (visibleChildren.isEmpty()) return padding[1] + padding[3]

        val minX = visibleChildren.minOf { it.x }
        val maxX = visibleChildren.maxOf { it.x + it.width }

        return (maxX - minX) + padding[3] + padding[1]
    }

    public override fun getAutoHeight(): Float {
        val visibleChildren = children.filter { it.visible && !it.isFloating }
        if (visibleChildren.isEmpty()) return padding[0] + padding[2]

        val minY = visibleChildren.minOf { it.y }
        val maxY = visibleChildren.maxOf { it.y + it.height }

        return (maxY - minY) + padding[0] + padding[2]
    }

    override fun renderChildren(mouseX: Float, mouseY: Float) {
        if (scrollable) {
            val contentX = x + padding[3]
            val contentY = y + padding[0]
            val viewWidth = width - padding[1] - padding[3]
            val viewHeight = height - padding[0] - padding[2]
            val buffer = 2f

            NVGRenderer.push()
            NVGRenderer.pushScissor(
                contentX - buffer,
                contentY - buffer,
                viewWidth + buffer * 2,
                viewHeight + buffer * 2
            )
            NVGRenderer.translate(0f, -scrollOffset)
        }

        children.forEach { child ->
            val oldX = child.xConstraint
            val oldY = child.yConstraint
            try {
                if(!child.isFloating) {
                    if(child.xPositionConstraint != Pos.MatchSibling && child.xPositionConstraint != Pos.ScreenPixels) child.xConstraint += padding[3]
                    if(child.yPositionConstraint != Pos.MatchSibling && child.yPositionConstraint != Pos.ScreenPixels) child.yConstraint += padding[0]
                }
                child.render(mouseX, mouseY)
            } finally {
                child.xConstraint = oldX
                child.yConstraint = oldY
            }
        }

        if (scrollable) {
            NVGRenderer.popScissor()
            NVGRenderer.pop()
        }
    }

    fun rotateTo(angle: Float, duration: Long = 300, type: EasingType = EasingType.EASE_OUT, onComplete: (() -> Unit)? = null): Rectangle {
        animateFloat({ rotation }, { rotation = it }, angle, duration, type, onComplete = onComplete)
        return this
    }

    fun dropShadow(): Rectangle = apply {
        dropShadow = true
    }

    open fun getScreenX(): Float = x

    open fun getScreenY(): Float {
        var totalScrollOffset = 0f
        var current = parent
        while (current != null) {
            if (current is Rectangle) totalScrollOffset += current.scrollOffset
            current = current.parent
        }
        return y - totalScrollOffset
    }

    open fun scrollable(enabled: Boolean): Rectangle = apply {
        scrollable = enabled
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

    open fun setGradientBorderColor(color1: Int, color2: Int): Rectangle = apply {
        borderColor = color1
        secondBorderColor = color2
    }

    open fun borderColor(color: Int): Rectangle = apply {
        borderColor = color
        secondBorderColor = color
    }

    open fun borderGradient(type: Gradient): Rectangle = apply {
        gradientType = type
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