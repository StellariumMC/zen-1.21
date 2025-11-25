package xyz.meowing.zen.config.ui.elements

import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.utils.render.NVGRenderer
import xyz.meowing.vexel.utils.style.Font
import xyz.meowing.zen.ui.Theme
import java.awt.Color

class FeatureTooltip : VexelElement<FeatureTooltip>() {
    var currentText: String = ""
    private val maxWidth = 350f
    private val padding = floatArrayOf(8f, 12f, 8f, 12f)
    private val fontSize = 14f
    private val lineHeight = 1.2f
    private val tokenRegex = Regex("§.|\\s+|[^\\s§]+")

    private val background = Rectangle(
        Theme.Bg.color,
        Theme.Border.color,
        6f,
        1f,
        padding
    )
        .setSizing(0f, Size.Pixels, 0f, Size.Pixels)
        .childOf(this)

    init {
        setSizing(0f, Size.Pixels, 0f, Size.Pixels)
        setPositioning(0f, Pos.ScreenPixels, 0f, Pos.ScreenPixels)
        visible = false
        setFloating()
    }

    fun setText(text: String) {
        currentText = text
        visible = text.isNotEmpty()
        updateSize()
    }

    private fun updateSize() {
        if (currentText.isEmpty()) {
            width = 0f
            height = 0f
            background.width = 0f
            background.height = 0f
            return
        }

        val bounds = NVGRenderer.wrappedTextBounds(
            currentText.stripColor(),
            maxWidth - padding[1] - padding[3],
            fontSize,
            NVGRenderer.defaultFont,
            lineHeight
        )

        val textHeight = bounds[3] - bounds[1]
        val contentWidth = maxWidth.coerceAtMost(bounds[2] - bounds[0] + padding[1] + padding[3])
        val contentHeight = textHeight + padding[0] + padding[2]

        width = contentWidth
        height = contentHeight
        background.width = contentWidth
        background.height = contentHeight

        cache.invalidate()
    }


    override fun onRender(mouseX: Float, mouseY: Float) {
        x = KnitMouse.Raw.x.toFloat() + 10f
        y = KnitMouse.Raw.y.toFloat() + 10f
    }

    override fun renderChildren(mouseX: Float, mouseY: Float) {
        super.renderChildren(mouseX, mouseY)
        if (currentText.isEmpty()) return

        val textX = x + padding[3]
        val textY = y + padding[0]

        drawMinecraftString(
            currentText,
            textX,
            textY,
            fontSize,
            maxWidth - padding[1] - padding[3],
            Theme.Text.color,
            NVGRenderer.defaultFont
        )
    }

    // thanks eclipse
    private fun drawMinecraftString(
        text: String,
        x: Float,
        y: Float,
        fontSize: Float,
        maxWidth: Float,
        defaultColor: Int,
        font: Font
    ) {
        var currentColor = defaultColor
        var currentY = y

        val tokens = tokenRegex.findAll(text).map { it.value }.toList()
        val lineWords = mutableListOf<Pair<String, Int>>()
        var lineWidth = 0f

        fun flushLine() {
            if (lineWords.isEmpty()) return
            var cursorX = x
            for ((word, color) in lineWords) {
                NVGRenderer.text(word, cursorX, currentY, fontSize, color, font)
                cursorX += NVGRenderer.textWidth(word, fontSize, font)
            }
            @Suppress("AssignedValueIsNeverRead") // intellij is fucking dumb
            currentY += fontSize * lineHeight
            lineWords.clear()
            lineWidth = 0f
        }

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                token.startsWith("§") && token.length == 2 -> {
                    currentColor = MCColor.getColor(token[1])
                    i++
                }
                token == "\n" -> {
                    flushLine()
                    i++
                }
                token.isBlank() -> {
                    val spaceWidth = NVGRenderer.textWidth(token, fontSize, font)
                    if (lineWidth + spaceWidth > maxWidth) {
                        flushLine()
                    } else {
                        lineWords.add(token to currentColor)
                        lineWidth += spaceWidth
                    }
                    i++
                }
                else -> {
                    val wordWidth = NVGRenderer.textWidth(token, fontSize, font)
                    if (lineWidth + wordWidth > maxWidth && lineWords.isNotEmpty()) {
                        flushLine()
                    }
                    lineWords.add(token to currentColor)
                    lineWidth += wordWidth
                    i++
                }
            }
        }
        flushLine()
    }

    private object MCColor {
        private val COLORS = mapOf(
            '0' to Color(0, 0, 0),
            '1' to Color(0, 0, 170),
            '2' to Color(0, 170, 0),
            '3' to Color(0, 170, 170),
            '4' to Color(170, 0, 0),
            '5' to Color(170, 0, 170),
            '6' to Color(255, 170, 0),
            '7' to Color(170, 170, 170),
            '8' to Color(85, 85, 85),
            '9' to Color(85, 85, 255),
            'a' to Color(85, 255, 85),
            'b' to Color(85, 255, 255),
            'c' to Color(255, 85, 85),
            'd' to Color(255, 85, 255),
            'e' to Color(255, 255, 85),
            'f' to Color(255, 255, 255)
        )

        fun getColor(code: Char): Int = COLORS[code]?.rgb ?: Color.WHITE.rgb
    }
}