package meowing.zen.canvas.core

import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.fadeIn
import meowing.zen.canvas.core.animations.fadeOut
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Tooltip
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
    var visible: Boolean = true

    var xConstraint: Float = 0f
    var yConstraint: Float = 0f

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false
    var isFloating: Boolean = false // If true, element is not considered in auto sizing of parents

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
    var onValueChange: ((Any) -> Unit)? = null

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
        children.filter { it.visible && !it.isFloating }.maxOfOrNull { it.x + it.width }?.coerceAtLeast(0f) ?: 0f

    protected open fun getAutoHeight(): Float =
        children.filter { it.visible && !it.isFloating }.maxOfOrNull { it.y + it.height }?.coerceAtLeast(0f) ?: 0f

    fun updateX() {
        val visibleParent = findFirstVisibleParent()

        x = when (xPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.x + (visibleParent.width * (xConstraint / 100f)) else xConstraint
            Pos.ScreenPercent -> screenWidth * (xConstraint / 100f)
            Pos.ParentPixels -> if(visibleParent != null) visibleParent.x + xConstraint else xConstraint
            Pos.ScreenPixels -> xConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) {
                    visibleParent.x + visibleParent.width / 2f - width / 2f
                } else xConstraint
            }
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
            Pos.ParentPixels -> if (visibleParent != null) visibleParent.y + yConstraint else yConstraint
            Pos.ScreenPixels -> yConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) {
                    visibleParent.y + visibleParent.height / 2f - height / 2f
                } else yConstraint
            }
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
                    if (prev is Rectangle) {
                        // The current janky solution to account for padding in rectangles, works for now
                        prev.y + yConstraint - (prev.padding[0] + prev.padding[2])
                    } else prev.y + yConstraint
                } else yConstraint
            }
        }
    }

    fun isPointInside(mouseX: Float, mouseY: Float): Boolean =
        mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    open fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> {
                onMouseEnter?.invoke(mouseX, mouseY)
                if(tooltipElement != null) {
                    tooltipElement!!.fadeIn(200, EasingType.EASE_OUT)
                    tooltipElement!!.innerText.fadeIn(200, EasingType.EASE_OUT)
                }
            }
            !isHovered && wasHovered -> {
                onMouseExit?.invoke(mouseX, mouseY)
                if(tooltipElement != null) {
                    tooltipElement!!.fadeOut(200, EasingType.EASE_OUT)
                    tooltipElement!!.innerText.fadeOut(200, EasingType.EASE_OUT)
                }
            }
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

        onRender(mouseX, mouseY)
        renderChildren(mouseX, mouseY)
    }

    protected open fun renderChildren(mouseX: Float, mouseY: Float) {
        children.forEach { it.render(mouseX, mouseY) }
    }

    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    fun childOf(parent: CanvasElement<*>): T = apply { parent.addChild(this) } as T

    @Suppress("UNCHECKED_CAST")
    fun addChild(child: CanvasElement<*>): T = apply {
        if (child.parent == null) {
            EventDispatcher.unregisterRoot(child)
        }
        child.parent = this
        children.add(child)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setSizing(widthType: Size, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setSizing(width: Float, widthType: Size, height: Float, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
        if (widthType == Size.Pixels) this.width = width else this.widthPercent = width
        if (heightType == Size.Pixels) this.height = height else this.heightPercent = height
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xConstraint: Pos, yConstraint: Pos): T = apply {
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xVal: Float, xPos: Pos, yVal: Float, yPos: Pos): T {
        this.xConstraint = xVal
        this.xPositionConstraint = xPos
        this.yConstraint = yVal
        this.yPositionConstraint = yPos
        return this as T
    }

    var tooltipElement: Tooltip? = null

    @Suppress("UNCHECKED_CAST")
    fun addTooltip(tooltip: String): T {
        tooltipElement = Tooltip().apply {
            innerText.text = tooltip
            childOf(this@CanvasElement)
        }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onHover(onEnter: (Float, Float) -> Unit, onExit: (Float, Float) -> Unit = { _, _ -> }): T = apply {
        this.onMouseEnter = onEnter
        this.onMouseExit = onExit
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        this.onMouseClick = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        this.onMouseRelease = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onKey(callback: (Int, Int, Int) -> Boolean): T = apply {
        this.onKeyPress = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onChar(callback: (Char) -> Boolean): T = apply {
        this.onCharType = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onValueChange(callback: (Any) -> Unit): T = apply {
        this.onValueChange = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        this.onMouseScroll = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun ignoreMouseEvents(): T = apply {
        this.onMouseClick = { _, _, _ -> false }
        this.onMouseRelease = { _, _, _ -> false }
        this.onMouseScroll = { _, _, _, _ -> false }
        this.onMouseMove = { _, _ -> }
        this.onMouseEnter = { _, _ -> }
        this.onMouseExit = { _, _ -> }
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setFloating(): T = apply {
        isFloating = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun show(): T = apply {
        visible = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun hide(): T = apply {
        visible = false
    } as T

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}