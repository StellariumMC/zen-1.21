package meowing.zen.ui.components

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Render2D.renderRoundedRect
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Modified version of the TextField class from NoammAddons
 * @author Noamm9
 */
class TextInputComponent(
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
    var radius: Float,
    val accentColor: Color,
    val hoverColor: Color,
    val placeholder: String = ""
) {
    var value = ""
        set(newVal) {
            if (field == newVal) return
            field = newVal
            cursorIndex = cursorIndex.coerceIn(0, field.length)
            selectionAnchor = selectionAnchor.coerceIn(0, field.length)
        }

    private val font = mc.textRenderer
    val textPadding = 4

    var focused = false
    private var isDragging = false
    private var caretVisible = true
    private var isHovered = false

    private val radius4f = Vector4f(radius, radius, radius, radius)
    private val normalBorderColor = Color(60, 60, 60)

    private var lastBlink = System.currentTimeMillis()
    private val caretBlinkRate = 500L

    private var cursorIndex = value.length
    private var selectionAnchor = value.length

    private val selectionStart: Int get() = min(cursorIndex, selectionAnchor)
    private val selectionEnd: Int get() = max(cursorIndex, selectionAnchor)
    private val hasSelection: Boolean get() = selectionStart != selectionEnd

    var scrollOffset = 0
    private var lastClickTime = 0L
    private var clickCount = 0

    fun draw(context: DrawContext, mouseX: Int, mouseY: Int) {
        isHovered = mouseX in x..(x + width) && mouseY in y..(y + height)

        val borderColor = when {
            focused -> accentColor
            isHovered -> hoverColor
            else -> normalBorderColor
        }

        renderRoundedRect(context, (x - 1).toFloat(), (y - 1).toFloat(), (width + 2).toFloat(), (height + 2).toFloat(), radius4f, borderColor)
        renderRoundedRect(context, (x).toFloat(), (y).toFloat(), (width).toFloat(), (height).toFloat(), radius4f, Color(20, 20, 20))

        val shouldShowPlaceholder = value.isEmpty() && !focused
        val textToRender = if (shouldShowPlaceholder) placeholder else value
        val textColor = if (shouldShowPlaceholder) Color(120, 120, 120).rgb else Color.WHITE.rgb
        val textY = y + (height - font.fontHeight) / 2

        context.enableScissor(x, y, x + width, y + height)

        if (hasSelection && !shouldShowPlaceholder) {
            val selStartStr = value.substring(0, selectionStart)
            val selEndStr = value.substring(0, selectionEnd)
            val x1 = x + textPadding - scrollOffset + font.getWidth(selStartStr)
            val x2 = x + textPadding - scrollOffset + font.getWidth(selEndStr)
            context.fill(x1, textY, x2, textY + font.fontHeight, accentColor.rgb)
        }

        context.drawText(font, textToRender, x + textPadding - scrollOffset, textY, textColor, false)

        if (focused && caretVisible && !shouldShowPlaceholder) {
            val textBeforeCaret = value.take(cursorIndex)
            val caretXPos = x + textPadding - scrollOffset + font.getWidth(textBeforeCaret)
            if (caretXPos >= x + textPadding - 1 && caretXPos <= x + textPadding + width - textPadding * 2) {
                context.fill(caretXPos, textY, caretXPos + 1, textY + font.fontHeight, Color.WHITE.rgb)
            }
        }

        context.disableScissor()

        if (System.currentTimeMillis() - lastBlink > caretBlinkRate) {
            caretVisible = !caretVisible
            lastBlink = System.currentTimeMillis()
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, button: Int): Boolean {
        if (button != 0) return false

        val clickedOnField = mouseX in x..(x + width) && mouseY in y..(y + height)

        if (clickedOnField) {
            focused = true
            isDragging = true

            val clickRelX = mouseX - (x + textPadding - scrollOffset)
            val newCursorIndex = getCharIndexAtAbsX(clickRelX)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 250) clickCount++
            else clickCount = 1

            lastClickTime = currentTime

            when (clickCount) {
                1 -> {
                    cursorIndex = newCursorIndex
                    if (!Screen.hasShiftDown()) {
                        selectionAnchor = cursorIndex
                    }
                }
                2 -> selectWordAt(newCursorIndex)
                else -> {
                    selectAll()
                    clickCount = 0
                }
            }
            resetCaretBlink()
            return true
        } else {
            focused = false
            isDragging = false
            return false
        }
    }

    fun keyTyped(keyCode: Int): Boolean {
        if (!focused) return false

        val ctrlDown = Screen.hasControlDown()
        val shiftDown = Screen.hasShiftDown()

        when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE -> {
                focused = false
                return true
            }
            GLFW.GLFW_KEY_ENTER -> {
                focused = false
                return true
            }
            GLFW.GLFW_KEY_BACKSPACE -> {
                if (ctrlDown) deletePrevWord()
                else deleteChar(-1)
                return true
            }
            GLFW.GLFW_KEY_DELETE -> {
                if (ctrlDown) deleteNextWord()
                else deleteChar(1)
                return true
            }
            GLFW.GLFW_KEY_LEFT -> {
                if (ctrlDown) moveWord(-1, shiftDown)
                else moveCaret(-1, shiftDown)
                return true
            }
            GLFW.GLFW_KEY_RIGHT -> {
                if (ctrlDown) moveWord(1, shiftDown)
                else moveCaret(1, shiftDown)
                return true
            }
            GLFW.GLFW_KEY_HOME -> {
                moveCaretTo(0, shiftDown)
                return true
            }
            GLFW.GLFW_KEY_END -> {
                moveCaretTo(value.length, shiftDown)
                return true
            }
            GLFW.GLFW_KEY_A -> {
                if (ctrlDown) {
                    selectAll()
                    return true
                }
            }
            GLFW.GLFW_KEY_C -> {
                if (ctrlDown) {
                    copySelection()
                    return true
                }
            }
            GLFW.GLFW_KEY_V -> {
                if (ctrlDown) {
                    paste()
                    return true
                }
            }
            GLFW.GLFW_KEY_X -> {
                if (ctrlDown) {
                    cutSelection()
                    return true
                }
            }
        }
        return false
    }

    fun charTyped(chr: Char): Boolean {
        if (!focused || chr.code < 32 || chr == 127.toChar()) return false
        insertText(chr.toString())
        return true
    }

    private fun resetCaretBlink() {
        lastBlink = System.currentTimeMillis()
        caretVisible = true
    }

    private fun getCharIndexAtAbsX(absClickX: Int): Int {
        if (absClickX <= 0) return 0
        var currentWidth = 0
        for (i in value.indices) {
            val charWidth = font.getWidth(value[i].toString())
            if (absClickX < currentWidth + charWidth / 2) {
                return i
            }
            currentWidth += charWidth
        }
        return value.length
    }

    private fun selectWordAt(pos: Int) {
        if (value.isEmpty()) return
        val currentPos = pos.coerceIn(0, value.length)

        if (currentPos < value.length && !value[currentPos].isWhitespace()) {
            var start = currentPos
            while (start > 0 && !value[start - 1].isWhitespace()) start--
            var end = currentPos
            while (end < value.length && !value[end].isWhitespace()) end++
            cursorIndex = end
            selectionAnchor = start
        } else {
            cursorIndex = currentPos
            selectionAnchor = currentPos
        }
        ensureCaretVisible()
    }

    private fun insertText(text: String) {
        val builder = StringBuilder(value)
        val newCursorPos = if (!hasSelection) cursorIndex
        else {
            val currentSelectionStart = selectionStart
            builder.delete(currentSelectionStart, selectionEnd)
            currentSelectionStart
        }

        builder.insert(newCursorPos, text)
        this.value = builder.toString()
        cursorIndex = (newCursorPos + text.length).coerceIn(0, this.value.length)
        selectionAnchor = cursorIndex

        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun deleteChar(direction: Int) {
        var textChanged = false
        var newText = value
        var newCursor = cursorIndex

        if (hasSelection) {
            val builder = StringBuilder(value)
            val selStart = selectionStart
            builder.delete(selStart, selectionEnd)
            newText = builder.toString()
            newCursor = selStart
            textChanged = true
        } else {
            if (direction == -1 && cursorIndex > 0) {
                val builder = StringBuilder(value)
                builder.deleteCharAt(cursorIndex - 1)
                newText = builder.toString()
                newCursor = cursorIndex - 1
                textChanged = true
            } else if (direction == 1 && cursorIndex < value.length) {
                val builder = StringBuilder(value)
                builder.deleteCharAt(cursorIndex)
                newText = builder.toString()
                textChanged = true
            }
        }

        if (textChanged) {
            this.value = newText
            cursorIndex = newCursor.coerceIn(0, this.value.length)
            selectionAnchor = cursorIndex

            val maxScroll = max(0, font.getWidth(this.value) - (width - textPadding * 2))
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll
            }

            ensureCaretVisible()
        }
        resetCaretBlink()
    }

    private fun moveCaret(amount: Int, shiftHeld: Boolean) {
        cursorIndex = (cursorIndex + amount).coerceIn(0, value.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun moveCaretTo(position: Int, shiftHeld: Boolean) {
        cursorIndex = position.coerceIn(0, value.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun moveWord(direction: Int, shiftHeld: Boolean) {
        cursorIndex = findWordBoundary(cursorIndex, direction)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun findWordBoundary(startIndex: Int, direction: Int): Int {
        var i = startIndex
        val len = value.length
        if (direction < 0) {
            if (i > 0) i--
            while (i > 0 && value[i].isWhitespace()) i--
            while (i > 0 && !value[i - 1].isWhitespace()) i--
        } else {
            while (i < len && !value[i].isWhitespace()) i++
            while (i < len && value[i].isWhitespace()) i++
        }
        return i.coerceIn(0, len)
    }

    private fun deletePrevWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == 0) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, -1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    private fun deleteNextWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == value.length) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, 1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    private fun selectAll() {
        selectionAnchor = 0
        cursorIndex = value.length
        resetCaretBlink()
    }

    private fun getSelectedText(): String {
        return if (hasSelection) value.substring(selectionStart, selectionEnd) else ""
    }

    private fun copySelection() {
        if (!hasSelection) return
        mc.keyboard.clipboard = getSelectedText()
    }

    private fun cutSelection() {
        if (!hasSelection) return
        copySelection()
        deleteChar(0)
    }

    private fun paste() {
        val clipboardText = mc.keyboard.clipboard
        if (clipboardText.isNotEmpty()) {
            insertText(clipboardText)
        }
    }

    private fun ensureCaretVisible() {
        val caretXAbsolute = font.getWidth(value.substring(0, cursorIndex.coerceIn(0, value.length)))
        val visibleTextStart = scrollOffset
        val visibleTextEnd = scrollOffset + (width - textPadding * 2)

        if (caretXAbsolute < visibleTextStart) {
            scrollOffset = caretXAbsolute
        } else if (caretXAbsolute > visibleTextEnd - 1) {
            scrollOffset = caretXAbsolute - (width - textPadding * 2) + 1
        }

        val maxScrollPossible = max(0, font.getWidth(value) - (width - textPadding * 2))
        scrollOffset = scrollOffset.coerceIn(0, maxScrollPossible)
        if (font.getWidth(value) <= width - textPadding * 2) {
            scrollOffset = 0
        }
    }
}