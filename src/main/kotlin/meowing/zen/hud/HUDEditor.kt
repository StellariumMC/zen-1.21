package meowing.zen.hud

import meowing.zen.hud.HUDManager.setPosition
import meowing.zen.utils.Utils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.roundToInt

class HUDEditor : Screen(Text.literal("HUD Editor")) {
    private val elements = mutableListOf<HUDElement>()
    private var dragging: HUDElement? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var selected: HUDElement? = null
    private var showGrid = true
    private var snapToGrid = true
    private var gridSize = 10
    private var previewMode = false
    private var showProperties = true
    private var showElements = true
    private val undoStack = mutableListOf<Map<String, HUDPosition>>()
    private val redoStack = mutableListOf<Map<String, HUDPosition>>()
    private var dirty = false

    override fun init() {
        super.init()
        elements.clear()
        loadElements()
        saveState()
        dirty = false
    }

    override fun close() {
        super.close()
        if (dirty) {
            elements.forEach { element ->
                setPosition(element.name, element.targetX, element.targetY, element.scale, element.enabled)
            }
        }
    }

    private fun loadElements() {
        HUDManager.getElements().forEach { (name, text) ->
            val lines = text.split("\n")
            val width = lines.maxOfOrNull { textRenderer.getWidth(it) } ?: 0
            val height = lines.size * textRenderer.fontHeight + 10
            elements.add(HUDElement(name, HUDManager.getX(name), HUDManager.getY(name), width + 10, height, text, HUDManager.getScale(name), HUDManager.isEnabled(name)))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)

        if (showGrid && !previewMode) drawGrid(context)
        elements.forEach { it.render(context, mouseX.toFloat(), mouseY.toFloat(), delta, previewMode) }

        if (!previewMode) {
            drawToolbar(context, mouseX, mouseY)
            if (showElements) drawElementList(context, mouseX, mouseY)
            if (showProperties) selected?.let { drawProperties(context, it) }
            drawTooltips(context)
        } else {
            drawPreviewHint(context)
        }

        super.render(context, mouseX, mouseY, delta)
    }

    private fun drawGrid(context: DrawContext) {
        val color = Color(60, 60, 80, 100).rgb

        for (x in 0 until width step gridSize) {
            context.drawVerticalLine(x, 0, height - 1, color)
        }

        for (y in 0 until height step gridSize) {
            context.drawHorizontalLine(0, width - 1, y, color)
        }
    }

    private fun drawPreviewHint(context: DrawContext) {
        val text = "Press P to exit preview mode"
        val textWidth = textRenderer.getWidth(text)
        val x = (width - textWidth) / 2
        val y = 10
        context.fill(x - 5, y - 3, x + textWidth + 5, y + 13, Color(0, 0, 0, 180).rgb)
        context.drawTextWithShadow(textRenderer, text, x, y, Color.WHITE.rgb)
    }

    private fun drawToolbar(context: DrawContext, mouseX: Int, mouseY: Int) {
        val height = 30
        context.fill(0, 0, width, height, Color(20, 20, 30, 220).rgb)
        context.fill(0, height, width, height + 2, Color(70, 130, 180, 255).rgb)

        val buttons = listOf("Grid", "Snap", "Preview", "Reset", "Properties", "Elements")
        val states = listOf(showGrid, snapToGrid, previewMode, false, showProperties, showElements)
        var x = 15

        val title = "Zen - HUD Editor"
        val textWidth = textRenderer.getWidth(title)
        val titlex = width - textWidth - 15

        context.drawTextWithShadow(textRenderer, title, titlex, 10, Color(100, 180, 255).rgb)

        buttons.forEachIndexed { index, button ->
            val buttonWidth = textRenderer.getWidth(button) + 20
            val hovered = mouseX in x..(x + buttonWidth) && mouseY in 0..height

            if (states[index]) {
                context.fill(x, height - 3, x + buttonWidth, height, Color(100, 180, 255).rgb)
            }

            val color = if (hovered) Color(100, 180, 255).rgb else Color(200, 220, 240).rgb
            context.drawTextWithShadow(textRenderer, button, x + 10, 10, color)
            x += buttonWidth + 10
        }
    }

    private fun drawElementList(context: DrawContext, mouseX: Int, mouseY: Int) {
        val listWidth = 200
        val elementHeight = 16
        val headerHeight = 25
        val padding = 10
        val listHeight = minOf(elements.size * elementHeight + headerHeight + padding, height - 100)
        val listX = width - listWidth - 15
        val listY = 40

        context.fill(listX, listY, listX + listWidth, listY + listHeight, Color(20, 20, 30, 180).rgb)
        drawHollowRect(context, listX, listY, listX + listWidth, listY + listHeight, Color(70, 130, 180, 255).rgb)

        context.drawTextWithShadow(textRenderer, "HUD Elements", listX + 10, listY + 8, Color(180, 220, 255).rgb)

        val scrollOffset = if (elements.size * elementHeight > listHeight - headerHeight - padding) {
            maxOf(0, elements.size * elementHeight - (listHeight - headerHeight - padding))
        } else 0

        elements.forEachIndexed { index, element ->
            val elementY = listY + headerHeight + index * elementHeight - scrollOffset
            if (elementY < listY + headerHeight || elementY > listY + listHeight - elementHeight) return@forEachIndexed

            val isSelected = element == selected
            val isHovered = mouseX in listX..(listX + listWidth) && mouseY in elementY..(elementY + elementHeight)

            when {
                isSelected -> context.fill(listX + 5, elementY - 1, listX + listWidth - 5, elementY + elementHeight - 1, Color(40, 90, 140, 150).rgb)
                isHovered -> context.fill(listX + 5, elementY - 1, listX + listWidth - 5, elementY + elementHeight - 1, Color(50, 50, 70, 80).rgb)
            }

            val nameColor = if (element.enabled) Color(220, 240, 255).rgb else Color(150, 150, 170).rgb
            val displayName = element.name.take(17) + if (element.name.length > 17) "..." else ""
            context.drawText(textRenderer, displayName, listX + 10, elementY + 3, nameColor, false)

            val toggleText = if (element.enabled) "ON" else "OFF"
            val toggleColor = if (element.enabled) Color(100, 220, 100).rgb else Color(220, 100, 100).rgb
            context.drawText(textRenderer, toggleText, listX + listWidth - 30, elementY + 3, toggleColor, false)
        }
    }

    private fun drawProperties(context: DrawContext, element: HUDElement) {
        val width = 140
        val height = 75
        val x = 15
        val y = this.height - height - 15

        context.fill(x, y, x + width, y + height, Color(20, 20, 30, 180).rgb)
        drawHollowRect(context, x, y, x + width, y + height, Color(70, 130, 180, 255).rgb)

        context.drawTextWithShadow(textRenderer, "Properties", x + 10, y + 10, Color(100, 180, 255).rgb)
        context.drawTextWithShadow(textRenderer, "Position: ${element.targetX.toInt()}, ${element.targetY.toInt()}", x + 15, y + 25, Color.WHITE.rgb)
        context.drawTextWithShadow(textRenderer, "Scale: ${"%.1f".format(element.scale)}", x + 15, y + 40, Color.WHITE.rgb)
        context.drawTextWithShadow(textRenderer, if (element.enabled) "§aEnabled" else "§cDisabled", x + 15, y + 55, Color.WHITE.rgb)
    }

    private fun drawTooltips(context: DrawContext) {
        val tooltip = when {
            selected != null -> "Scroll to scale, Arrow keys to move"
            else -> null
        }

        tooltip?.let { text ->
            val x = (width - textRenderer.getWidth(text)) / 2
            val y = height - 30
            context.fill(x - 5, y - 3, x + textRenderer.getWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
            context.drawTextWithShadow(textRenderer, text, x, y, Color(100, 180, 255).rgb)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (selected != null) {
            saveState()
            val scaleDelta = if (verticalAmount > 0) 0.1f else -0.1f
            selected!!.scale = (selected!!.scale + scaleDelta).coerceIn(0.2f, 5f)
            dirty = true
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            if (!handleToolbarClick(mouseX.toInt(), mouseY.toInt()) &&
                (!showElements || !handleElementListClick(mouseX.toInt(), mouseY.toInt()))) {
                handleElementDrag(mouseX.toInt(), mouseY.toInt())
            }
        } else if (button == 1) {
            elements.reversed().find { it.isMouseOver(mouseX.toFloat(), mouseY.toFloat()) }?.let {
                selected = it
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun handleToolbarClick(mouseX: Int, mouseY: Int): Boolean {
        if (mouseY > 30) return false

        val buttons = listOf("Grid", "Snap", "Preview", "Reset", "Properties", "Elements")
        var x = 15

        buttons.forEach { button ->
            val buttonWidth = textRenderer.getWidth(button) + 20
            if (mouseX in x..(x + buttonWidth)) {
                when (button) {
                    "Grid" -> showGrid = !showGrid
                    "Snap" -> snapToGrid = !snapToGrid
                    "Preview" -> previewMode = !previewMode
                    "Reset" -> resetAll()
                    "Properties" -> showProperties = !showProperties
                    "Elements" -> showElements = !showElements
                }
                return true
            }
            x += buttonWidth + 10
        }
        return false
    }

    private fun handleElementListClick(mouseX: Int, mouseY: Int): Boolean {
        val listWidth = 200
        val listX = width - listWidth - 15
        val listY = 40
        val elementHeight = 16
        val headerHeight = 25

        if (mouseX !in listX..(listX + listWidth)) return false

        val clickedIndex = (mouseY - listY - headerHeight) / elementHeight
        if (clickedIndex !in 0 until elements.size) return false

        val element = elements[clickedIndex]
        if (mouseX >= listX + listWidth - 40) {
            element.enabled = !element.enabled
            dirty = true
        } else {
            selected = element
        }
        return true
    }

    private fun handleElementDrag(mouseX: Int, mouseY: Int) {
        if (previewMode) return

        elements.reversed().find { it.isMouseOver(mouseX.toFloat(), mouseY.toFloat()) }?.let { element ->
            dragging = element
            selected = element
            dragOffsetX = mouseX - element.getRenderX(Utils.getPartialTicks())
            dragOffsetY = mouseY - element.getRenderY(Utils.getPartialTicks())
            saveState()
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        dragging?.let { element ->
            var newX = mouseX - dragOffsetX
            var newY = mouseY - dragOffsetY

            if (snapToGrid) {
                newX = (newX / gridSize).roundToInt() * gridSize.toDouble()
                newY = (newY / gridSize).roundToInt() * gridSize.toDouble()
            }

            newX = newX.coerceIn(0.0, (width - element.width).toDouble())
            newY = newY.coerceIn(0.0, (height - element.height).toDouble())

            element.setPosition(newX.toFloat(), newY.toFloat())
            dirty = true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        dragging?.let { element ->
            dragging = null
            dirty = true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE -> {
                if (previewMode) previewMode = false
                else close()
                return false
            }
            GLFW.GLFW_KEY_G -> showGrid = !showGrid
            GLFW.GLFW_KEY_P -> previewMode = !previewMode
            GLFW.GLFW_KEY_R -> resetAll()
            GLFW.GLFW_KEY_Z -> if (hasControlDown()) undo()
            GLFW.GLFW_KEY_Y -> if (hasControlDown()) redo()
            GLFW.GLFW_KEY_DELETE -> selected?.let { delete(it) }
            GLFW.GLFW_KEY_UP -> selected?.let { move(it, 0, -1) }
            GLFW.GLFW_KEY_DOWN -> selected?.let { move(it, 0, 1) }
            GLFW.GLFW_KEY_LEFT -> selected?.let { move(it, -1, 0) }
            GLFW.GLFW_KEY_RIGHT -> selected?.let { move(it, 1, 0) }
            GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> selected?.let { scale(it, 0.1f) }
            GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> selected?.let { scale(it, -0.1f) }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun scale(element: HUDElement, delta: Float) {
        saveState()
        element.scale = (element.scale + delta).coerceIn(0.2f, 5f)
        dirty = true
    }

    private fun move(element: HUDElement, deltaX: Int, deltaY: Int) {
        saveState()
        val moveAmount = if (hasShiftDown()) 10 else 1
        val newX = element.targetX + deltaX * moveAmount
        val newY = element.targetY + deltaY * moveAmount

        val clampedX = newX.coerceIn(0f, (width - element.width).toFloat())
        val clampedY = newY.coerceIn(0f, (height - element.height).toFloat())

        element.setPosition(clampedX, clampedY)
        dirty = true
    }

    private fun delete(element: HUDElement) {
        saveState()
        element.enabled = false
        selected = null
        dirty = true
    }

    private fun resetAll() {
        saveState()
        elements.forEach { element ->
            element.setPosition(50f, 50f)
            element.enabled = true
            element.scale = 1f
        }
        dirty = true
    }

    private fun saveState() {
        val state = elements.associate { it.name to HUDPosition(it.targetX, it.targetY, it.scale, it.enabled) }
        undoStack.add(state)
        if (undoStack.size > 20) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun undo() {
        if (undoStack.size > 1) {
            redoStack.add(undoStack.removeLast())
            applyState(undoStack.last())
            dirty = true
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val state = redoStack.removeLast()
            undoStack.add(state)
            applyState(state)
            dirty = true
        }
    }

    private fun applyState(state: Map<String, HUDPosition>) {
        elements.forEach { element ->
            state[element.name]?.let { pos ->
                element.setPosition(pos.x, pos.y)
                element.scale = pos.scale
                element.enabled = pos.enabled
            }
        }
    }

    private fun drawHollowRect(context: DrawContext, x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        context.fill(x1, y1, x2, y1 + 1, color)
        context.fill(x1, y2 - 1, x2, y2, color)
        context.fill(x1, y1, x1 + 1, y2, color)
        context.fill(x2 - 1, y1, x2, y2, color)
    }

    override fun shouldPause() = false
    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {}
}