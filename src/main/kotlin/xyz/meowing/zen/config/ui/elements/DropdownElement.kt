package xyz.meowing.zen.config.ui.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateSize
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.utils.render.NVGRenderer
import xyz.meowing.zen.config.ui.elements.utils.fadeIn
import xyz.meowing.zen.config.ui.elements.utils.fadeOut
import xyz.meowing.zen.ui.Theme
import xyz.meowing.zen.config.ui.panels.SectionButton

class DropdownElement(
    name: String,
    private val options: List<String>,
    selectedIndex: Int
) : VexelElement<DropdownElement>() {

    companion object {
        var openDropdown: DropdownElement? = null
        fun closeAllDropdowns() = openDropdown?.collapse()
    }

    private var expanded = false
    private var isAnimating = false
    var selectedIndex = selectedIndex
        private set

    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 8f, Pos.ParentPixels)
        .childOf(this)

    private val selectedButton = Rectangle(Theme.BgLight.color, Theme.Border.color, 5f, 1f)
        .setPositioning(-6f, Pos.ParentPixels, 0f, Pos.MatchSibling)
        .alignRight()
        .setOffset(0f, -2f)
        .childOf(this)

    private val selectedText = Text(options[selectedIndex], Theme.Text.color, 16f)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(selectedButton)

    private val optionsContainer = Rectangle(Theme.BgLight.color, 0x00000000, 5f, 0f)
        .setSizing(228f, Size.Pixels, 0f, Size.Pixels)
        .setPositioning(6f, Pos.ParentPixels, 32f, Pos.ParentPixels)
        .childOf(this)

    private val actualContainer = Container()
        .setSizing(Size.Fill, Size.Auto)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
        .childOf(optionsContainer)

    init {
        setSizing(240f, Size.Pixels, 32f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)

        updateButtonSize()

        selectedButton.onHover(
            { _, _ -> selectedButton.colorTo(Theme.Highlight.color, 150, EasingType.EASE_OUT) },
            { _, _ -> selectedButton.colorTo(Theme.BgLight.color, 150, EasingType.EASE_IN) }
        )

        selectedButton.onClick { _, _, button ->
            when (button) {
                0 -> {
                    if (!isAnimating) {
                        if (expanded) collapse() else expand()
                    }
                    true
                }
                1 -> {
                    cycleOption()
                    true
                }
                else -> false
            }
        }

        optionsContainer.visible = false
        createOptions()
    }

    private fun createOptions() {
        options.forEachIndexed { index, option ->
            val isFirst = index == 0
            val isLast = index == options.size - 1

            val topLeftRadius = if (isFirst) 5f else 0f
            val topRightRadius = if (isFirst) 5f else 0f
            val bottomLeftRadius = if (isLast) 5f else 0f
            val bottomRightRadius = if (isLast) 5f else 0f

            val optionRect = Rectangle(0x00000000, 0x00000000, 0f, 0f)
                .borderRadiusVarying(topRight = topRightRadius, topLeft = topLeftRadius, bottomRight = bottomRightRadius, bottomLeft = bottomLeftRadius)
                .setSizing(228f, Size.Pixels, 26f, Size.Pixels)
                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
                .childOf(actualContainer)

            val optionText = Text(option, Theme.Text.color, 16f)
                .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
                .childOf(optionRect)

            optionText.visible = false

            optionRect.onHover(
                { _, _ -> optionRect.colorTo(Theme.Highlight.color, 150, EasingType.EASE_OUT) },
                { _, _ -> optionRect.colorTo(0x00000000, 150, EasingType.EASE_IN) }
            )

            optionRect.onClick { _, _, _ ->
                selectOption(index, option)
                true
            }
        }
    }

    private fun updateButtonSize() {
        val textWidth = NVGRenderer.textWidth(selectedText.text, 16f, NVGRenderer.defaultFont)
        selectedButton.setSizing(textWidth + 12f, Size.Pixels, 20f, Size.Pixels)
    }

    private fun expand() {
        if (expanded) return

        closeAllDropdowns()
        openDropdown = this

        expanded = true
        isAnimating = true

        optionsContainer.visible = true
        val targetHeight = 32f + options.size * 26f + 6f
        val containerHeight = options.size * 26f

        animateSize(240f, targetHeight, 200, EasingType.EASE_OUT) {
            isAnimating = false
            invalidateParentLayout()
        }
        optionsContainer.animateSize(228f, containerHeight, 200, EasingType.EASE_OUT) {
            optionsContainer.children.forEach { child ->
                child.fadeIn(50)
                child.children.forEach {
                    it.visible = true
                    it.fadeIn(50)
                }
            }
        }
    }

    private fun collapse() {
        if (!expanded) return

        expanded = false
        isAnimating = true
        openDropdown = null

        animateSize(240f, 32f, 200, EasingType.EASE_IN) {
            isAnimating = false
            invalidateParentLayout()
        }

        optionsContainer.children.forEach { child ->
            child.fadeOut(50)
            child.children.forEach { it.fadeOut(50) }
        }

        optionsContainer.animateSize(228f, 0f, 200, EasingType.EASE_IN) {
            optionsContainer.visible = false
        }
    }

    private fun selectOption(index: Int, option: String) {
        selectedIndex = index
        selectedText.text = option
        updateButtonSize()
        onValueChange.forEach { it.invoke(selectedIndex) }
        if (!isAnimating) collapse()
    }

    private fun cycleOption() {
        val nextIndex = if (selectedIndex >= options.size - 1) 0 else selectedIndex + 1
        selectOption(nextIndex, options[nextIndex])
    }

    @Suppress("UNCHECKED_CAST")
    private fun invalidateParentLayout() {
        var current = parent as? VexelElement<SectionButton>

        while (current != null && current !is SectionButton) {
            current = current.parent as? VexelElement<SectionButton>
        }

        current?.recalculateHeight()
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}