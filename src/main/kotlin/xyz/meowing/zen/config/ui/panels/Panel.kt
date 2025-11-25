package xyz.meowing.zen.config.ui.panels

import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateFloat
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.managers.config.CategoryElement
import xyz.meowing.zen.ui.Theme
import kotlin.math.max

class Panel(
    private val category: CategoryElement,
    initialX: Float,
    initialY: Float,
    private val onConfigUpdate: (String, Any) -> Unit
) : VexelElement<Panel>() {

    private var dragging = false
    private var deltaX = 0f
    private var deltaY = 0f
    private var extended = true
    private var isAnimating = false

    private val background = Rectangle(
        Theme.BgLight.withAlpha(0.04f),
        0x00000000,
        5f,
        0f,
        padding = floatArrayOf(HEADER_HEIGHT, 0f, 0f, 0f)
    )
        .setSizing(WIDTH, Size.Pixels, 0f, Size.Auto)
        .setMaxAutoSize(null, 800f)
        .scrollable(true)
        .showScrollbar(false)
        .dropShadow(10f, 3f, Theme.BgDark.color)
        .childOf(this)

    private val headerContainer = Rectangle(0x00000000, 0x00000000, 0f, 0f)
        .setSizing(WIDTH, Size.Pixels, HEADER_HEIGHT, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
        .childOf(this)

    private val header = Rectangle(Theme.Bg.color, 0x00000000, 5f, 0f)
        .setSizing(WIDTH, Size.Pixels, HEADER_HEIGHT, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
        .childOf(headerContainer)

    private val headerBottomLeftFill = Rectangle(Theme.Bg.color, 0x00000000, 0f, 0f)
        .setSizing(5f, Size.Pixels, 5f, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, HEADER_HEIGHT - 5f, Pos.ParentPixels)
        .childOf(headerContainer)

    private val headerBottomRightFill = Rectangle(Theme.Bg.color, 0x00000000, 0f, 0f)
        .setSizing(5f, Size.Pixels, 5f, Size.Pixels)
        .setPositioning(WIDTH - 5f, Pos.ParentPixels, HEADER_HEIGHT - 5f, Pos.ParentPixels)
        .childOf(headerContainer)

    private val titleText = Text(category.name, Theme.Text.color, 22f)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(header)

    private val headerSeparator = Rectangle(Theme.TextMuted.color, 0x00000000, 0f, 0f)
        .setPositioning(0f, Pos.ParentCenter, HEADER_HEIGHT - 0.5f, Pos.ParentPixels)
        .setSizing(WIDTH + 0.5f, Size.Pixels, 0.5f, Size.Pixels)
        .childOf(headerContainer)

    private val sectionsContainer = Rectangle(0x00000000, 0x00000000, 0f, 0f)
        .setSizing(WIDTH, Size.Pixels, 0f, Size.Auto)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
        .childOf(background)

    private val sections = mutableListOf<SectionButton>()

    companion object {
        const val WIDTH = 240f
        const val HEADER_HEIGHT = 32f
        private val panelPositions = mutableMapOf<String, Pair<Float, Float>>()
    }

    init {
        setSizing(WIDTH, Size.Pixels, 0f, Size.Auto)

        val savedPosition = panelPositions[category.name]
        if (savedPosition != null) {
            setPositioning(savedPosition.first, Pos.ScreenPixels, savedPosition.second, Pos.ScreenPixels)
        } else {
            setPositioning(initialX, Pos.ScreenPixels, initialY, Pos.ScreenPixels)
        }

        header.onClick { _, _, button ->
            if (button == 0) {
                deltaX = x - KnitMouse.Raw.x.toFloat()
                deltaY = y - KnitMouse.Raw.y.toFloat()
                dragging = true
                true
            } else false
        }

        header.onMouseRelease { _, _, button ->
            if (button == 0) {
                dragging = false
                savePosition()
            }
            true
        }

        category.features.sortedBy { it.featureName }.forEachIndexed { index, feature ->
            val isLast = index == category.features.size - 1
            sections.add(SectionButton(feature, sectionsContainer, onConfigUpdate, isLast))
        }

        updateVisibility()
    }

    private fun savePosition() {
        panelPositions[category.name] = Pair(x, y)
    }

    private fun updateVisibility() {
        sections.forEach { it.updateVisibility(extended) }
    }

    fun adjustScrollAfterResize() {
        val contentHeight = background.getContentHeight()
        val viewHeight = background.height - background.padding[0] - background.padding[2]
        val maxScroll = max(0f, contentHeight - viewHeight)

        if (background.scrollOffset > maxScroll) {
            isAnimating = true
            background.animateFloat(
                { background.scrollOffset },
                { background.scrollOffset = it },
                maxScroll,
                100,
                EasingType.EASE_OUT,
                onComplete = {
                    isAnimating = false
                }
            )
        }
    }

    fun matchesSearch(query: String): Boolean {
        if (query.isEmpty()) return true
        return sections.any { it.matchesSearch(query) }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (dragging) {
            x = deltaX + KnitMouse.Raw.x.toFloat()
            y = deltaY + KnitMouse.Raw.y.toFloat()
        }

        sections
            .filter { !it.isPointInside(mouseX, mouseY) && isHovered }
            .forEach { it.handleMouseMove(mouseX, mouseY) }

        if (!isAnimating) adjustScrollAfterResize()
    }
}