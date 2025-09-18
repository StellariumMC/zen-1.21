package meowing.zen.canvas.core

import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.components.Rectangle
import net.minecraft.client.util.Window

enum class Size {
    Auto,
    ParentPerc,
    Pixels
}

enum class Pos {
    ParentPercent,
    ScreenPercent,
    ParentPixels,
    ScreenPixels,
    ParentCenter,
    ScreenCenter,
    AfterSibling,
    MatchSibling
}

abstract class CanvasElement<T : CanvasElement<T>>(
    var widthType: Size = Size.Pixels,
    var heightType: Size = Size.Pixels
) {
    protected val children: MutableList<CanvasElement<*>> = mutableListOf()

    var xPositionConstraint = Pos.ParentPixels
    var yPositionConstraint = Pos.ParentPixels
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var widthPercent: Float = 100f
    var heightPercent: Float = 100f
    var absoluteX: Float = 0f
    var absoluteY: Float = 0f
    var visible: Boolean = true

    // Add these fields to your CanvasElement class
    var xConstraint: Float = 0f
    var yConstraint: Float = 0f

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false

    val window: Window get() = mc.window
    val screenWidth: Int get() = window.width
    val screenHeight: Int get() = window.height

    var parent: CanvasElement<*>? = null

    var onMouseEnter: ((Float, Float) -> Unit)? = null
    var onMouseExit: ((Float, Float) -> Unit)? = null
    var onMouseMove: ((Float, Float) -> Unit)? = null
    var onMouseScroll: ((Float, Float, Double, Double) -> Boolean)? = null
    var onMouseClick: ((Float, Float, Int) -> Boolean)? = null
    var onMouseRelease: ((Float, Float, Int) -> Boolean)? = null
    var onKeyPress: ((Int, Int, Int) -> Boolean)? = null
    var onKeyRelease: ((Int, Int, Int) -> Boolean)? = null
    var onCharType: ((Char) -> Boolean)? = null

    init {
        if (parent == null) {
            EventDispatcher.registerRoot(this)
        }
    }

    fun destroy() {
        if (parent == null) {
            EventDispatcher.unregisterRoot(this)
        }
        children.forEach { it.destroy() }
        children.clear()
    }

    fun findFirstVisibleParent(): CanvasElement<*>? {
        var current = parent
        while (current != null) {
            if (current.visible) return current
            current = current.parent
        }
        return null
    }

    open fun updateWidth() {
        width = when (widthType) {
            Size.Auto -> getAutoWidth()
            Size.ParentPerc -> {
                if(parent == null) {
                    screenWidth * (widthPercent / 100f)
                }
                else findFirstVisibleParent()?.width?.times(widthPercent / 100f) ?: width
            }
            Size.Pixels -> width
        }
    }

    open fun updateHeight() {
        height = when (heightType) {
            Size.Auto -> getAutoHeight()
            Size.ParentPerc -> {
                if(parent == null) {
                    screenHeight * (heightPercent / 100f)
                }
                else findFirstVisibleParent()?.height?.times(heightPercent / 100f) ?: height
            }
            Size.Pixels -> height
        }
    }

    protected open fun getAutoWidth(): Float =
        children.filter { it.visible }.maxOfOrNull { it.x + it.width }?.coerceAtLeast(0f) ?: 0f

    protected open fun getAutoHeight(): Float =
        children.filter { it.visible }.maxOfOrNull { it.y + it.height }?.coerceAtLeast(0f) ?: 0f

    fun updateX() {
        val visibleParent = findFirstVisibleParent()

        x = when (xPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.x + (visibleParent.width * (xConstraint / 100f)) else xConstraint
            Pos.ScreenPercent -> screenWidth * (xConstraint / 100f)
            Pos.ParentPixels -> xConstraint
            Pos.ScreenPixels -> xConstraint
            Pos.ParentCenter -> if (visibleParent != null) visibleParent.x + (visibleParent.width / 2f) - (width / 2f) + xConstraint else xConstraint
            Pos.ScreenCenter -> (screenWidth / 2f) - (width / 2f) + xConstraint
            Pos.AfterSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    prev.x + prev.width + xConstraint
                } else xConstraint
            }
            Pos.MatchSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    prev.x
                } else xConstraint
            }
        }
    }

    fun updateY() {
        val visibleParent = findFirstVisibleParent()

        y = when (yPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.y + (visibleParent.height * (yConstraint / 100f)) else yConstraint
            Pos.ScreenPercent -> screenHeight * (yConstraint / 100f)
            Pos.ParentPixels -> yConstraint
            Pos.ScreenPixels -> yConstraint
            Pos.ParentCenter -> if (visibleParent != null) visibleParent.y + (visibleParent.height / 2f) - (height / 2f) + yConstraint else yConstraint
            Pos.ScreenCenter -> (screenHeight / 2f) - (height / 2f) + yConstraint
            Pos.AfterSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    (prev.y + prev.height + yConstraint)
                } else yConstraint
            }
            Pos.MatchSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    if(prev is Rectangle) {
                        // The current janky solution to account for padding in rectangles, works for now
                        prev.y + yConstraint - (prev.padding[0] + prev.padding[2])
                    } else prev.y + yConstraint
                } else yConstraint
            }
        }
    }

    fun isPointInside(mouseX: Float, mouseY: Float): Boolean =
        mouseX >= absoluteX && mouseX <= absoluteX + width && mouseY >= absoluteY && mouseY <= absoluteY + height

    open fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> onMouseEnter?.invoke(mouseX, mouseY)
            !isHovered && wasHovered -> onMouseExit?.invoke(mouseX, mouseY)
        }

        if (isHovered) onMouseMove?.invoke(mouseX, mouseY)

        return children.reversed().any { it.handleMouseMove(mouseX, mouseY) } || isHovered
    }

    open fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseClick(mouseX, mouseY, button) }

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                onMouseClick?.invoke(mouseX, mouseY, button) ?: true
            }
            else -> false
        }
    }

    open fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val wasPressed = isPressed
        isPressed = false

        val childHandled = children.reversed().any { it.handleMouseRelease(mouseX, mouseY, button) }
        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) && (onMouseRelease?.invoke(mouseX, mouseY, button) ?: true))
    }

    open fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
        return childHandled || (isPointInside(mouseX, mouseY) && (onMouseScroll?.invoke(mouseX, mouseY, horizontal, vertical) ?: false))
    }

    open fun handleKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible || !isFocused) return false
        return onKeyPress?.invoke(keyCode, scanCode, modifiers) ?: false || children.reversed().any { it.handleKeyPress(keyCode, scanCode, modifiers) }
    }

    open fun handleKeyRelease(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible || !isFocused) return false
        return onKeyRelease?.invoke(keyCode, scanCode, modifiers) ?: false || children.reversed().any { it.handleKeyRelease(keyCode, scanCode, modifiers) }
    }

    open fun handleCharType(char: Char): Boolean {
        if (!visible || !isFocused) return false
        return onCharType?.invoke(char) ?: false || children.reversed().any { it.handleCharType(char) }
    }

    fun focus() {
        parent?.children?.forEach { it.isFocused = false }
        isFocused = true
    }

    fun unfocus() {
        isFocused = false
    }

    open fun render(mouseX: Float, mouseY: Float) {
        if (!visible) return

        updateHeight()
        updateWidth()
        updateX()
        updateY()

        val visibleParent = findFirstVisibleParent()
        absoluteX = (visibleParent?.absoluteX ?: 0f) + x
        absoluteY = (visibleParent?.absoluteY ?: 0f) + y

        onRender(mouseX, mouseY)
        renderChildren(mouseX, mouseY)
    }

    protected open fun renderChildren(mouseX: Float, mouseY: Float) {
        children.forEach { it.render(mouseX, mouseY) }
    }

    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    fun childOf(parent: CanvasElement<*>): T = apply { parent.addChild(this) } as T

    fun addChild(child: CanvasElement<*>): T = apply {
        if (child.parent == null) {
            EventDispatcher.unregisterRoot(child)
        }
        child.parent = this
        children.add(child)
    } as T

    fun setSizing(widthType: Size, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
    } as T

    fun setSizing(width: Float, widthType: Size, height: Float, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
        if (widthType == Size.Pixels) this.width = width else this.widthPercent = width
        if (heightType == Size.Pixels) this.height = height else this.heightPercent = height
    } as T

    fun setPositioning(xConstraint: Pos, yConstraint: Pos): T = apply {
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
    } as T

    fun setPositioning(xVal: Float, xPos: Pos, yVal: Float, yPos: Pos): T {
        this.xConstraint = xVal
        this.xPositionConstraint = xPos
        this.yConstraint = yVal
        this.yPositionConstraint = yPos
        return this as T
    }

    fun onHover(onEnter: (Float, Float) -> Unit, onExit: (Float, Float) -> Unit = { _, _ -> }): T = apply {
        this.onMouseEnter = onEnter
        this.onMouseExit = onExit
    } as T

    fun onClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        this.onMouseClick = callback
    } as T

    fun onRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        this.onMouseRelease = callback
    } as T

    fun onKey(callback: (Int, Int, Int) -> Boolean): T = apply {
        this.onKeyPress = callback
    } as T

    fun onChar(callback: (Char) -> Boolean): T = apply {
        this.onCharType = callback
    } as T

    fun onScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        this.onMouseScroll = callback
    } as T

    fun show(): T = apply { visible = true } as T
    fun hide(): T = apply { visible = false } as T

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}