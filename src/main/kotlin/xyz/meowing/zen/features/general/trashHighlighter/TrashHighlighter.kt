package xyz.meowing.zen.features.general.trashHighlighter

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import net.minecraft.world.item.ItemStack
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.Zen.LOGGER
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

@Module
object TrashHighlighter : Feature(
    "trashHighlighter",
    true
) {
    private val highlightColor by ConfigDelegate<Color>("trashHighlighter.color")
    private val highlightType by ConfigDelegate<Int>("trashHighlighter.type")

    private val defaultList = listOf(
        FilteredItem("CRYPT_DREADLORD_SWORD", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("MACHINE_GUN_BOW", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("Healing VIII", TrashFilterType.CONTAINS, TrashInputType.DISPLAY_NAME),
        FilteredItem("DUNGEON_LORE_PAPER", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("ENCHANTED_BONE", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("CRYPT_BOW", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("(?:SKELETON|ZOMBIE)_(?:GRUNT|MASTER|SOLDIER)_(?:BOOTS|LEGGINGS|CHESTPLATE|HELMET)", TrashFilterType.REGEX, TrashInputType.ITEM_ID),
        FilteredItem("SUPER_HEAVY", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("INFLATABLE_JERRY", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("DUNGEON_TRAP", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("SNIPER_HELMET", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("SKELETOR", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("ROTTEN", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("HEAVY", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("PREMIUM_FLESH", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("TRAINING", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("CONJURING_SWORD", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("FEL_PEARL", TrashFilterType.EQUALS, TrashInputType.ITEM_ID),
        FilteredItem("ZOMBIE_KNIGHT", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID),
        FilteredItem("ENCHANTED_ROTTEN_FLESH", TrashFilterType.CONTAINS, TrashInputType.ITEM_ID)
    )

    private val trashData = StoredFile("features/TrashHighlighter")
    private var trashFilters: List<FilteredItem> by trashData.list("filters", FilteredItem.CODEC, defaultList)

    data class FilteredItem(
        var textInput: String,
        val selectedFilter: TrashFilterType,
        val selectedInput: TrashInputType
    ) {
        fun matches(stack: ItemStack): Boolean {
            val input = when (selectedInput) {
                TrashInputType.ITEM_ID -> stack.skyblockID
                TrashInputType.DISPLAY_NAME -> stack.hoverName.string
                TrashInputType.LORE -> stack.lore.joinToString(" ")
            }

            return when (selectedFilter) {
                TrashFilterType.CONTAINS -> input.contains(textInput)
                TrashFilterType.EQUALS -> input == textInput
                TrashFilterType.REGEX -> try { input.matches(textInput.toRegex()) } catch (_: Exception) { false }
            }
        }

        companion object {
            val CODEC: Codec<FilteredItem> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("textInput").forGetter { it.textInput },
                    Codec.STRING.xmap(
                        { TrashFilterType.valueOf(it) },
                        { it.name }
                    ).fieldOf("selectedFilter").forGetter { it.selectedFilter },
                    Codec.STRING.xmap(
                        { TrashInputType.valueOf(it) },
                        { it.name }
                    ).fieldOf("selectedInput").forGetter { it.selectedInput }
                ).apply(instance, ::FilteredItem)
            }
        }
    }

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Trash highlighter",
                "Highlights the trash items",
                "General",
                ConfigElement(
                    "trashHighlighter",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Highlight color",
                ConfigElement(
                    "trashHighlighter.color",
                    ElementType.ColorPicker(Color(255, 0, 0, 127))
                )
            )
            .addFeatureOption(
                "Highlight type",
                ConfigElement(
                    "trashHighlighter.type",
                    ElementType.Dropdown(listOf("Slot", "Border"), 0)
                )
            )
            .addFeatureOption(
                "Trash highlighter filter GUI",
                ConfigElement(
                    "trashHighlighter.guiButton",
                    ElementType.Button("Open Filter GUI") {
                        TickScheduler.Client.post {
                            client.setScreen(TrashFilterGui())
                        }
                    }
                )
            )
    }

    override fun initialize() {
        register<GuiEvent.Slot.Render> { event ->
            if (!event.slot.hasItem()) return@register
            val stack = event.slot.item ?: return@register
            val context = event.context
            val x = event.slot.x
            val y = event.slot.y

            if (stack.skyblockID.isNotEmpty() && isTrashItem(stack)) {
                val highlightColor = highlightColor.rgb

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
            LOGGER.warn($$"Error in Trash Highlighter$getFilter: $$e")
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