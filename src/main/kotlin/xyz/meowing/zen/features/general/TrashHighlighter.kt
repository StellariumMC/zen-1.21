package xyz.meowing.zen.features.general

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.ui.constraint.ChildHeightConstraint
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.TickUtils
import net.minecraft.item.ItemStack
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.Zen.LOGGER
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

enum class FilterType { REGEX, EQUALS, CONTAINS }
enum class InputType { ITEM_ID, DISPLAY_NAME, LORE }

@Module
object TrashHighlighter : Feature("trashhighlighter", true) {
    private val highlightType by ConfigDelegate<Int>("trashhighlighttype")
    private val color by ConfigDelegate<Color>("trashhighlightercolor")

    private val defaultList = listOf(
        FilteredItem("CRYPT_DREADLORD_SWORD", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("MACHINE_GUN_BOW", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("Healing VIII", FilterType.CONTAINS, InputType.DISPLAY_NAME),
        FilteredItem("DUNGEON_LORE_PAPER", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("ENCHANTED_BONE", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("CRYPT_BOW", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("(?:SKELETON|ZOMBIE)_(?:GRUNT|MASTER|SOLDIER)_(?:BOOTS|LEGGINGS|CHESTPLATE|HELMET)", FilterType.REGEX, InputType.ITEM_ID),
        FilteredItem("SUPER_HEAVY", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("INFLATABLE_JERRY", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("DUNGEON_TRAP", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("SNIPER_HELMET", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("SKELETOR", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("ROTTEN", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("HEAVY", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("PREMIUM_FLESH", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("TRAINING", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("CONJURING_SWORD", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("FEL_PEARL", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("ZOMBIE_KNIGHT", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("ENCHANTED_ROTTEN_FLESH", FilterType.CONTAINS, InputType.ITEM_ID)
    )

    private val trashData = StoredFile("features/TrashHighlighter")
    private var trashFilters: List<FilteredItem> by trashData.list("filters", FilteredItem.CODEC, defaultList)

    data class FilteredItem(
        var textInput: String,
        val selectedFilter: FilterType,
        val selectedInput: InputType
    ) {
        fun matches(stack: ItemStack): Boolean {
            val input = when (selectedInput) {
                InputType.ITEM_ID -> stack.skyblockID
                InputType.DISPLAY_NAME -> stack.name.string
                InputType.LORE -> stack.lore.joinToString(" ")
            }

            return when (selectedFilter) {
                FilterType.CONTAINS -> input.contains(textInput)
                FilterType.EQUALS -> input == textInput
                FilterType.REGEX -> try { input.matches(textInput.toRegex()) } catch (_: Exception) { false }
            }
        }

        companion object {
            val CODEC: Codec<FilteredItem> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("textInput").forGetter { it.textInput },
                    Codec.STRING.xmap(
                        { FilterType.valueOf(it) },
                        { it.name }
                    ).fieldOf("selectedFilter").forGetter { it.selectedFilter },
                    Codec.STRING.xmap(
                        { InputType.valueOf(it) },
                        { it.name }
                    ).fieldOf("selectedInput").forGetter { it.selectedInput }
                ).apply(instance, ::FilteredItem)
            }
        }
    }

    override fun addConfig() {
        ConfigManager
            .addFeature("Trash Highlighter", "", "General", ConfigElement(
                "trashhighlighter",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Highlight color", "Highlight color", "Color", ConfigElement(
                "trashhighlightercolor",
                ElementType.ColorPicker(Color(255, 0, 0, 127))
            ))
            .addFeatureOption("Highlight type", "Highlight type", "Type", ConfigElement(
                "trashhighlighttype",
                ElementType.Dropdown(listOf("Slot", "Border"), 0)
            ))
            .addFeatureOption("Trash Highlighter Filter GUI", "Trash Highlighter Filter GUI", "GUI", ConfigElement(
                "trashhighlightguibutton",
                ElementType.Button("Open Filter GUI") {
                    TickUtils.schedule(2) {
                        client.setScreen(TrashFilterGui())
                    }
                }
            ))
    }

    override fun initialize() {
        register<GuiEvent.Slot.Render> { event ->
            if (!event.slot.hasStack()) return@register
            val stack = event.slot.stack ?: return@register
            val context = event.context
            val x = event.slot.x
            val y = event.slot.y

            if (stack.skyblockID.isNotEmpty() && isTrashItem(stack)) {
                val highlightColor = color.rgb

                when (highlightType) {
                    0 -> context.fill(x, y, x + 16, y + 16, highlightColor)
                    1 -> {
                        context.fill(x, y, x + 16, y + 1, highlightColor)
                        context.fill(x, y, x + 1, y + 16, highlightColor)
                        context.fill(x + 15, y, x + 16, y + 16, highlightColor)
                        context.fill(x, y + 15, x + 16, y + 16, highlightColor)
                    }
                }
            }
        }
    }

    private fun safeGetFilters(): List<FilteredItem> {
        return try {
            trashFilters
        } catch (e: Exception) {
            LOGGER.warn("Error in Trash Highlighter\$getFilter: $e")
            emptyList()
        }
    }

    private fun isTrashItem(stack: ItemStack): Boolean {
        return safeGetFilters().any { filter ->
            try { filter.matches(stack) } catch (_: Exception) { false }
        }
    }

    fun getFilters(): List<FilteredItem> = safeGetFilters()
    fun setFilters(filters: List<FilteredItem>) {
        trashFilters = filters
    }
    fun resetToDefault() {
        trashFilters = defaultList
    }
}

class TrashHighlightText(
    initialValue: String = "",
    placeholder: String = "",
    var onChange: ((String) -> Unit)? = null
) : UIContainer() {
    var text: String = initialValue
    val input: UITextInput
    private val placeholderText: UIText?
    private var onInputCallback: ((String) -> Unit)? = null

    init {
        val container = UIRoundedRectangle(3f).constrain {
            x = 1.pixels()
            y = 1.pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(Color(18, 24, 28, 255)) childOf this

        input = (UITextInput(text).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 16.pixels()
            height = 10.pixels()
        }.setColor(Color(170, 230, 240, 255)) childOf container) as UITextInput

        placeholderText = (if (placeholder.isNotEmpty()) {
            UIText(placeholder).constrain {
                x = 8.pixels()
                y = CenterConstraint()
            }.setColor(Color(80, 120, 140, 255)) childOf container
        } else null) as UIText?

        updatePlaceholderVisibility()
        setupEventHandlers()
    }

    private fun setupEventHandlers() {
        onMouseClick {
            input.setText(text)
            input.grabWindowFocus()
        }

        input.onKeyType { _, _ ->
            text = input.getText()
            updatePlaceholderVisibility()
            onChange?.invoke(text)
            onInputCallback?.invoke(text)
        }

        input.onFocusLost {
            text = input.getText()
            onChange?.invoke(text)
        }
    }

    private fun updatePlaceholderVisibility() {
        placeholderText?.let { placeholder ->
            if (text.isEmpty()) placeholder.unhide(true)
            else placeholder.hide(true)
        }
    }
}

class TrashFilterGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(12, 16, 20, 255)
        val accent = Color(100, 245, 255, 255)
        val accent2 = Color(80, 200, 220, 255)
        val success = Color(47, 102, 47, 255)
        val danger = Color(115, 41, 41, 255)
        val buttonGroup = Color(16, 20, 24, 255)
        val buttonSelected = Color(70, 180, 200, 255)
        val buttonHover = Color(20, 70, 75, 255)
        val divider = Color(30, 35, 40, 255)
    }

    private lateinit var scrollComponent: ScrollComponent
    private lateinit var listContainer: UIContainer
    private lateinit var inputField: TrashHighlightText

    init {
        buildGui()
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 70.percent()
            height = 80.percent()
        }.setColor(theme.accent2) childOf window

        val main = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.bg) childOf border

        createHeader(main)
        createContent(main)
        createFooter(main)
        renderFilters()
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 40.pixels()
        } childOf parent

        UIText("§lTrash Filter").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.8.pixels()
        }.setColor(theme.accent) childOf header

        val resetButton = createBlock(3f).constrain {
            x = 8.pixels(true)
            y = CenterConstraint()
            width = 24.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf header

        resetButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, Color.RED.darker().toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            TrashHighlighter.resetToDefault()
            renderFilters()
            KnitChat.fakeMessage("§aReset filters to default!")
        }

        UIText("⟲").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.2.pixels()
        }.setColor(theme.accent) childOf resetButton

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf header
    }

    private fun createContent(parent: UIComponent) {
        val contentPanel = UIContainer().constrain {
            x = 8.pixels()
            y = 48.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 96.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        listContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(4f)
        } childOf scrollComponent
    }

    private fun createFooter(parent: UIComponent) {
        val footer = UIContainer().constrain {
            x = 8.pixels()
            y = 100.percent() - 40.pixels()
            width = 100.percent() - 16.pixels()
            height = 40.pixels()
        } childOf parent

        createBlock(0f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf footer

        inputField = TrashHighlightText("", "Enter filter pattern...").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 80.pixels()
            height = 24.pixels()
        } childOf footer

        inputField.input.onKeyType { _, keyCode ->
            if (keyCode == UKeyboard.KEY_ENTER) addFilter()
        }

        val addButton = createBlock(3f).constrain {
            x = 100.percent() - 64.pixels()
            y = CenterConstraint()
            width = 56.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf footer

        addButton.onMouseEnter {
            if (inputField.text.isEmpty()) {
                animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
                return@onMouseEnter
            }
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.success.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            addFilter()
            inputField.input.setText("")
        }

        UIText("Add").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(theme.accent) childOf addButton
    }

    private fun renderFilters() {
        listContainer.clearChildren()
        val filters = TrashHighlighter.getFilters()

        if (filters.isEmpty()) {
            UIText("No filters added...").constrain {
                x = CenterConstraint()
                y = 20.pixels()
                textScale = 1f.pixels()
            }.setColor(theme.accent2.withAlpha(128)) childOf listContainer
            return
        }

        filters.forEachIndexed { index, filter ->
            createFilterRow(index, filter)
        }
    }

    private fun createFilterRow(index: Int, filter: TrashHighlighter.FilteredItem) {
        val row = createBlock(3f).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(4f)
            width = 100.percent()
            height = 36.pixels()
        }.setColor(theme.element) childOf listContainer

        val textInput = TrashHighlightText(filter.textInput).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 45.5.percent()
            height = 24.pixels()
        } childOf row

        textInput.onChange = { text ->
            updateFilter(index, text, filter.selectedFilter, filter.selectedInput)
        }

        createInputTypeButtons(row, index, filter)
        createFilterTypeButtons(row, index, filter)
        createDeleteButton(row, index)
    }

    private fun createInputTypeButtons(parent: UIComponent, index: Int, filter: TrashHighlighter.FilteredItem) {
        val container = createBlock(2f).constrain {
            x = 48.percent()
            y = CenterConstraint()
            width = 18.percent()
            height = 20.pixels()
        }.setColor(theme.buttonGroup) childOf parent

        val options = listOf(
            InputType.ITEM_ID to "ID",
            InputType.DISPLAY_NAME to "Name",
            InputType.LORE to "Lore"
        )

        options.forEachIndexed { optionIndex, (type, name) ->
            createButtonGroupOption(
                name,
                filter.selectedInput == type,
                optionIndex == 0,
                optionIndex == options.size - 1
            ) {
                updateFilter(index, filter.textInput, filter.selectedFilter, type)
            }.constrain {
                x = (optionIndex * 33.3).percent()
                y = 0.percent()
                width = 33.3.percent()
                height = 100.percent()
            } childOf container

            if (optionIndex < options.size - 1) {
                createBlock(0f).constrain {
                    x = ((optionIndex + 1) * 50).percent()
                    y = 2.pixels()
                    width = 1.pixels()
                    height = 100.percent() - 4.pixels()
                }.setColor(theme.divider) childOf container
            }
        }
    }

    private fun createFilterTypeButtons(parent: UIComponent, index: Int, filter: TrashHighlighter.FilteredItem) {
        val container = createBlock(2f).constrain {
            x = 67.percent()
            y = CenterConstraint()
            width = 28.percent()
            height = 20.pixels()
        }.setColor(theme.buttonGroup) childOf parent

        val options = listOf(
            FilterType.CONTAINS to "Contains",
            FilterType.EQUALS to "Equals",
            FilterType.REGEX to "Regex"
        )

        options.forEachIndexed { optionIndex, (type, name) ->
            createButtonGroupOption(
                name,
                filter.selectedFilter == type,
                optionIndex == 0,
                optionIndex == options.size - 1
            ) {
                updateFilter(index, filter.textInput, type, filter.selectedInput)
            }.constrain {
                x = (optionIndex * 33.33).percent()
                y = 0.percent()
                width = 33.33.percent()
                height = 100.percent()
            } childOf container

            if (optionIndex < options.size - 1) {
                createBlock(0f).constrain {
                    x = ((optionIndex + 1) * 33.33).percent()
                    y = 2.pixels()
                    width = 1.pixels()
                    height = 100.percent() - 4.pixels()
                }.setColor(theme.divider) childOf container
            }
        }
    }

    private fun createButtonGroupOption(
        text: String,
        selected: Boolean,
        isFirst: Boolean,
        isLast: Boolean,
        onClick: () -> Unit
    ): UIComponent {
        val radius = when {
            isFirst -> 2f
            isLast -> 2f
            else -> 0f
        }

        val buttonBorder = createBlock(radius).setColor(theme.buttonSelected)

        val button = createBlock(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
            color = theme.buttonGroup.constraint
        } childOf buttonBorder

        buttonBorder.setColor(if (selected) theme.buttonSelected else Color(0,0,0,0))

        if (!selected) {
            button.onMouseEnter {
                buttonBorder.animate { setColorAnimation(Animations.OUT_EXP, 0.2f, theme.buttonHover.toConstraint()) }
            }.onMouseLeave {
                buttonBorder.animate { setColorAnimation(Animations.OUT_EXP, 0.2f, Color(0,0,0,0).toConstraint()) }
            }
        }

        button.onMouseClick {
            if (!selected) onClick()
        }

        UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(if (selected) Color.WHITE else theme.accent) childOf button

        return buttonBorder
    }

    private fun createDeleteButton(parent: UIComponent, index: Int) {
        val deleteButton = createBlock(3f).constrain {
            x = 100.percent() - 28.pixels()
            y = CenterConstraint()
            width = 20.pixels()
            height = 20.pixels()
        }.setColor(theme.element) childOf parent

        deleteButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick { removeFilter(index) }

        UIText("✕").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(Color.RED.darker()) childOf deleteButton
    }

    private fun addFilter() {
        val pattern = inputField.text.trim()
        if (pattern.isEmpty()) {
            KnitChat.fakeMessage("§cEnter a pattern!")
            return
        }

        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        currentFilters.add(TrashHighlighter.FilteredItem(pattern, FilterType.CONTAINS, InputType.ITEM_ID))
        TrashHighlighter.setFilters(currentFilters)
        inputField.text = ""
        renderFilters()
    }

    private fun updateFilter(index: Int, text: String, filterType: FilterType, inputType: InputType) {
        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        if (index < currentFilters.size) {
            currentFilters[index] = TrashHighlighter.FilteredItem(text, filterType, inputType)
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

    private fun Color.withAlpha(alpha: Int): Color = Color(red, green, blue, alpha)
}