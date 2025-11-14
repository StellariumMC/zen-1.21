package xyz.meowing.zen.features.general.trashHighlighter

import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateFloat
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.core.VexelScreen
import xyz.meowing.vexel.elements.Button
import xyz.meowing.vexel.elements.TextInput
import xyz.meowing.zen.ui.Theme
import kotlin.math.max

class TrashFilterGui : VexelScreen("Trash Filter") {
    private lateinit var listContainer: Container
    private lateinit var inputField: TextInput
    private var isAnimating = false
    private val filterRows = mutableMapOf<Int, FilterRow>()

    data class FilterRow(
        val container: Rectangle,
        val textInput: TextInput,
        val inputButtons: List<Button>,
        val filterButtons: List<Button>,
        val deleteButton: Button
    )

    override fun afterInitialization() {
        val mainContainer = Rectangle(Theme.BgDark.color, Theme.Border.color, 8f, 1f)
            .setSizing(70f, Size.ParentPerc, 80f, Size.ParentPerc)
            .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
            .padding(16f)
            .dropShadow(40f, 2f, Theme.BgDark.color)
            .childOf(window)

        createHeader(mainContainer)
        createContent(mainContainer)
        createFooter(mainContainer)
        renderFilters()
    }

    override fun onRenderGui() {
        if (!isAnimating) adjustScrollAfterResize()
    }

    private fun createHeader(parent: Rectangle) {
        val header = Container()
            .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(parent)

        Text("Trash Filter", Theme.Text.color, 24f)
            .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
            .setOffset(0f, -8f)
            .childOf(header)

        Button("Reset", Theme.Text.color, fontSize = 16f)
            .setSizing(32f, Size.Pixels, 32f, Size.Pixels)
            .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
            .setOffset(0f, -8f)
            .alignRight()
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                TrashHighlighter.resetToDefault()
                renderFilters()
                KnitChat.fakeMessage("§aReset filters to default!")
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.Danger.color, 150L) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color, 150L) }
                )
            }
            .childOf(header)

        Rectangle(Theme.Border.color, 0, 0f, 0f)
            .setSizing(100f, Size.ParentPerc, 1f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 4f, Pos.AfterSibling)
            .childOf(header)
    }

    private fun createContent(parent: Rectangle) {
        listContainer = Container(floatArrayOf(0f, 0f, 12f, 0f), scrollable = true)
            .setSizing(100f, Size.ParentPerc, 87.5f, Size.Fill)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .scrollbarColor(Theme.Highlight.color)
            .scrollbarRadius(4f)
            .scrollbarIgnorePadding(true)
            .scrollbarCustomPadding(-10f)
            .childOf(parent)
    }

    private fun createFooter(parent: Rectangle) {
        Rectangle(Theme.Border.color, 0, 0f, 0f)
            .setSizing(100f, Size.ParentPerc, 1f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(parent)

        val footer = Container(floatArrayOf(12f, 0f, 0f, 0f))
            .setSizing(100f, Size.ParentPerc, 48f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(parent)

        inputField = TextInput("", "Enter filter pattern...", hoverColor = null)
            .setSizing(0f, Size.Fill, 36f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .fontSize(13f)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .padding(10f)
            .childOf(footer)
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.BgLight.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color) }
                )
            }

        inputField.onCharType { keyCode, _, _ ->
            if (keyCode == KnitKeys.KEY_ENTER.code) {
                addFilter()
                true
            } else false
        }

        val addButton = Button("Add", Theme.Text.color, fontSize = 13f)
            .setSizing(64f, Size.Pixels, 36f, Size.Pixels)
            .setPositioning(8f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                addFilter()
                true
            }
            .childOf(footer)

        addButton.onHover(
            onEnter = { _, _ ->
                val color = if (inputField.value.isEmpty()) Theme.Danger.color else Theme.Success.color
                addButton.background.colorTo(color, 300L)
            },
            onExit = { _, _ ->
                addButton.background.colorTo(Theme.Bg.color, 300L)
            }
        )
    }

    private fun renderFilters() {
        listContainer.children.toList().forEach { it.destroy() }
        filterRows.clear()

        val filters = TrashHighlighter.getFilters()

        if (filters.isEmpty()) {
            Text("No filters added...", Theme.TextMuted.color, 14f)
                .setPositioning(0f, Pos.ParentCenter, 40f, Pos.ParentPixels)
                .childOf(listContainer)
            return
        }

        filters.forEachIndexed { index, filter ->
            createFilterRow(index, filter)
        }
    }

    private fun createFilterRow(index: Int, filter: TrashHighlighter.FilteredItem) {
        val row = Rectangle(Theme.Bg.color, Theme.Border.color, 6f, 1f)
            .setSizing(100f, Size.ParentPerc, 52f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, if (index == 0) 0f else 8f, Pos.AfterSibling)
            .padding(12f)
            .childOf(listContainer)

        val textInput = TextInput(filter.textInput, "Pattern", hoverColor = null)
            .setSizing(50.3f, Size.ParentPerc, 28f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .fontSize(12f)
            .backgroundColor(Theme.BgLight.color)
            .borderColor(Theme.BorderMuted.color)
            .borderRadius(4f)
            .padding(8f)
            .childOf(row)
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.Bg.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.BgLight.color) }
                )
            }

        textInput.onValueChange { text ->
            updateFilter(index, text as String, filter.selectedFilter, filter.selectedInput)
        }

        val inputButtons = createInputTypeButtons(row, index, filter)
        val filterButtons = createFilterTypeButtons(row, index, filter)
        val deleteButton = createDeleteButton(row, index)

        filterRows[index] = FilterRow(row, textInput, inputButtons, filterButtons, deleteButton)
    }

    private fun createInputTypeButtons(parent: Rectangle, index: Int, filter: TrashHighlighter.FilteredItem): List<Button> {
        val container = Rectangle(Theme.BgLight.color, Theme.BorderMuted.color, 4f, 1f)
            .setSizing(18f, Size.ParentPerc, 28f, Size.Pixels)
            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .childOf(parent)

        val types = listOf(
            TrashInputType.ITEM_ID to "ID",
            TrashInputType.DISPLAY_NAME to "Name",
            TrashInputType.LORE to "Lore"
        )

        return types.mapIndexed { i, (type, name) ->
            val isSelected = filter.selectedInput == type

            Button(name, if (isSelected) 0xFFFFFFFF.toInt() else Theme.TextMuted.color, fontSize = 11f)
                .setSizing(33.33f, Size.ParentPerc, 100f, Size.ParentPerc)
                .setPositioning(if (i == 0) 0f else 0f, if (i == 0) Pos.ParentPixels else Pos.AfterSibling, 0f, Pos.ParentPixels)
                .backgroundColor(if (isSelected) Theme.Highlight.color else Theme.BgLight.color)
                .borderColor(0)
                .hoverColors(bg = Theme.Primary.color, text = 0xFFFFFFFF.toInt())
                .padding(4f)
                .onClick { _, _, _ ->
                    updateFilter(index, filter.textInput, filter.selectedFilter, type)
                    true
                }
                .childOf(container)
                .apply {
                    background.borderRadiusVarying(
                        topLeft = if (i == types.size - 1) 4f else 0f,
                        bottomLeft = if (i == 0) 4f else 0f,
                        topRight = if (i == 0) 4f else 0f,
                        bottomRight = if (i == types.size - 1) 4f else 0f
                    )
                }
        }
    }

    private fun createFilterTypeButtons(parent: Rectangle, index: Int, filter: TrashHighlighter.FilteredItem): List<Button> {
        val container = Rectangle(Theme.BgLight.color, Theme.BorderMuted.color, 4f, 1f)
            .setSizing(28f, Size.ParentPerc, 28f, Size.Pixels)
            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.ParentCenter)
            .childOf(parent)

        val types = listOf(
            TrashFilterType.CONTAINS to "Contains",
            TrashFilterType.EQUALS to "Equals",
            TrashFilterType.REGEX to "Regex"
        )

        return types.mapIndexed { i, (type, name) ->
            val isSelected = filter.selectedFilter == type

            Button(name, if (isSelected) 0xFFFFFFFF.toInt() else Theme.TextMuted.color, fontSize = 11f)
                .setSizing(33.33f, Size.ParentPerc, 100f, Size.ParentPerc)
                .setPositioning(0f, Pos.AfterSibling, 0f, Pos.ParentPixels)
                .backgroundColor(if (isSelected) Theme.Highlight.color else Theme.BgLight.color)
                .borderColor(0)
                .hoverColors(bg = Theme.Primary.color, text = 0xFFFFFFFF.toInt())
                .padding(4f)
                .onClick { _, _, _ ->
                    updateFilter(index, filter.textInput, type, filter.selectedInput)
                    true
                }
                .childOf(container)
                .apply {
                    background.borderRadiusVarying(
                        topLeft = if (i == types.size - 1) 4f else 0f,
                        bottomLeft = if (i == 0) 4f else 0f,
                        topRight = if (i == 0) 4f else 0f,
                        bottomRight = if (i == types.size - 1) 4f else 0f
                    )
                }
        }
    }

    private fun createDeleteButton(parent: Rectangle, index: Int): Button {
        val deleteButton = Button("×", Theme.Danger.color, fontSize = 18f)
            .setSizing(32f, Size.Pixels, 28f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .alignRight()
            .backgroundColor(Theme.BgLight.color)
            .borderColor(Theme.BorderMuted.color)
            .borderRadius(4f)
            .hoverColors(null, Theme.Text.color)
            .padding(4f)
            .onClick { _, _, _ ->
                removeFilter(index)
                true
            }
            .childOf(parent)

        deleteButton.onHover(
            onEnter = { _, _ -> deleteButton.background.colorTo(Theme.Danger.color, 150L) },
            onExit = { _, _ -> deleteButton.background.colorTo(Theme.BgLight.color, 150L) }
        )

        return deleteButton
    }

    private fun addFilter() {
        val pattern = inputField.value.trim()
        if (pattern.isEmpty()) {
            KnitChat.fakeMessage("§cEnter a pattern!")
            return
        }

        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        currentFilters.add(TrashHighlighter.FilteredItem(pattern, TrashFilterType.CONTAINS, TrashInputType.ITEM_ID))
        TrashHighlighter.setFilters(currentFilters)
        inputField.value = ""
        renderFilters()
    }

    private fun updateFilter(index: Int, text: String, trashFilterType: TrashFilterType, trashInputType: TrashInputType) {
        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        if (index < currentFilters.size) {
            currentFilters[index] = TrashHighlighter.FilteredItem(text, trashFilterType, trashInputType)
            TrashHighlighter.setFilters(currentFilters)
            renderFilters()
        }
    }

    private fun removeFilter(index: Int) {
        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        if (index < currentFilters.size) {
            currentFilters.removeAt(index)
            TrashHighlighter.setFilters(currentFilters)
            renderFilters()
        }
    }

    private fun adjustScrollAfterResize() {
        val contentHeight = listContainer.getContentHeight()
        val viewHeight = listContainer.height - listContainer.padding[0] - listContainer.padding[2]
        val maxScroll = max(0f, contentHeight - viewHeight)

        if (listContainer.scrollOffset > maxScroll) {
            isAnimating = true
            listContainer.animateFloat(
                { listContainer.scrollOffset },
                { listContainer.scrollOffset = it },
                maxScroll,
                100,
                EasingType.EASE_OUT,
                onComplete = { isAnimating = false }
            )
        }
    }
}