package xyz.meowing.zen.config.ui.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateSize
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.ui.Theme
import xyz.meowing.zen.config.ui.panels.SectionButton

class MultiCheckboxElement(
    name: String,
    private val options: List<String>,
    selectedIndices: Set<Int>
) : VexelElement<MultiCheckboxElement>() {

    companion object {
        var openMultiCheckbox: MultiCheckboxElement? = null
        fun closeAllMultiCheckboxes() = openMultiCheckbox?.collapse()
    }

    private var expanded = false
    private var isAnimating = false
    var selectedIndices = selectedIndices.toMutableSet()
        private set

    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 8f, Pos.ParentPixels)
        .childOf(this)

    private val selectedButton = Rectangle(Theme.BgLight.color, Theme.Border.color, 5f, 1f)
        .setPositioning(-6f, Pos.ParentPixels, 0f, Pos.MatchSibling)
        .alignRight()
        .setOffset(0f, -2f)
        .childOf(this)

    private val selectedText = Text("${selectedIndices.size} selected", Theme.Text.color, 16f)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(selectedButton)

    private val optionsContainer = Rectangle(Theme.BgLight.color, 0x00000000, 5f, 0f)
        .setSizing(228f, Size.Pixels, 0f, Size.Pixels)
        .setPositioning(6f, Pos.ParentPixels, 37f, Pos.ParentPixels)
        .childOf(this)

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
                else -> false
            }
        }

        optionsContainer.visible = false
        createOptions()
    }

    private fun createOptions() {
        options.forEachIndexed { index, option ->
            val optionRect = Rectangle(0x00000000, 0x00000000, 0f, 0f, padding = floatArrayOf(0f, 0f, 1.5f, 0f))
                .setSizing(228f, Size.Pixels, 26f, Size.Pixels)
                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
                .childOf(optionsContainer)

            val checkbox = Rectangle(Theme.BgLight.color, Theme.Text.color, 3f, 1f)
                .setSizing(12f, Size.Pixels, 12f, Size.Pixels)
                .setPositioning(6f, Pos.ParentPixels, 0f, Pos.ParentCenter)
                .childOf(optionRect)

            val checkmark = Rectangle(Theme.Primary.color, 0x00000000, 2f, 0f)
                .setSizing(8f, Size.Pixels, 8f, Size.Pixels)
                .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
                .childOf(checkbox)

            checkmark.visible = selectedIndices.contains(index)

            Text(option, Theme.Text.color, 14f)
                .setPositioning(24f, Pos.ParentPixels, 0f, Pos.ParentCenter)
                .childOf(optionRect)

            optionRect.onHover(
                { _, _ -> optionRect.colorTo(Theme.Highlight.color, 150, EasingType.EASE_OUT) },
                { _, _ -> optionRect.colorTo(0x00000000, 150, EasingType.EASE_IN) }
            )

            optionRect.onClick { _, _, _ ->
                toggleOption(index, checkmark, checkbox)
                true
            }
        }
    }

    private fun updateButtonSize() {
        selectedButton.setSizing(100f, Size.Pixels, 20f, Size.Pixels)
    }

    private fun expand() {
        if (expanded) return

        closeAllMultiCheckboxes()
        openMultiCheckbox = this

        expanded = true
        isAnimating = true

        optionsContainer.visible = true
        val targetHeight = 32f + options.size * 26f + 12f
        val containerHeight = options.size * 26f + 6f

        animateSize(240f, targetHeight, 200, EasingType.EASE_OUT) {
            isAnimating = false
            invalidateParentLayout()
        }
        optionsContainer.animateSize(228f, containerHeight, 200, EasingType.EASE_OUT)
    }

    private fun collapse() {
        if (!expanded) return

        expanded = false
        isAnimating = true
        openMultiCheckbox = null

        animateSize(240f, 32f, 200, EasingType.EASE_IN) {
            optionsContainer.visible = false
            isAnimating = false
            invalidateParentLayout()
        }
        optionsContainer.animateSize(228f, 0f, 200, EasingType.EASE_IN)
    }

    private fun toggleOption(index: Int, checkmark: VexelElement<*>, checkbox: Rectangle) {
        val wasSelected = selectedIndices.contains(index)

        if (wasSelected) {
            selectedIndices.remove(index)
            checkmark.visible = false
        } else {
            selectedIndices.add(index)
            checkmark.visible = true
            checkbox.colorTo(Theme.Primary.withAlpha(0.3f), 100, EasingType.EASE_OUT) {
                checkbox.colorTo(Theme.BgLight.color, 100, EasingType.EASE_IN)
            }
        }

        selectedText.text = "${selectedIndices.size} selected"
        onValueChange.forEach { it.invoke(selectedIndices.toSet()) }
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