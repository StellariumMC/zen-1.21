package xyz.meowing.zen.features.hud
//
//import xyz.meowing.zen.Zen
//import xyz.meowing.zen.config.ConfigDelegate
//import xyz.meowing.zen.config.ui.elements.base.ElementType
//import xyz.meowing.zen.events.GuiEvent
//import xyz.meowing.zen.features.Feature
//import xyz.meowing.zen.utils.ItemUtils.lore
//import xyz.meowing.zen.utils.Utils.removeFormatting
//import net.minecraft.client.gui.screen.ingame.HandledScreen
//import net.minecraft.item.AirBlockItem
//import xyz.meowing.vexel.components.base.Pos
//import xyz.meowing.vexel.components.base.Size
//import xyz.meowing.vexel.elements.TextInput
//import xyz.meowing.vexel.utils.render.NVGRenderer
//import xyz.meowing.zen.config.ConfigElement
//import xyz.meowing.zen.config.ConfigManager
//import java.awt.Color
//
//@Zen.Module
//object InventorySearch : Feature("inventorysearch") {
//    private val searchLore by ConfigDelegate<Boolean>("inventorysearchlore")
//    private val highlightType by ConfigDelegate<Int>("inventorysearchtype")
//    private val color by ConfigDelegate<Color>("inventorysearchcolor")
////    private var mathResult: String? = null
////    private val searchInput = TextInputComponent(
////        placeholder = "Search...",
////        x = 0,
////        y = 0,
////        width = 200,
////        height = 25,
////        radius = 3f,
////        accentColor = Color(170, 230, 240),
////        hoverColor = Color(70, 120, 140)
////    )
//    private val searchIn = TextInput(
//        backgroundColor = Color(20, 20, 20).rgb,
//        borderColor = Color(60, 60, 60).rgb,
//        hoverColor = Color(20, 20, 20).rgb
//    )
//        .setPositioning(0f, Pos.ScreenCenter, 95f, Pos.ScreenPercent)
//        .setSizing(400f, Size.Pixels, 35f, Size.Pixels)
//
//    init {
//        searchIn.onHover(
//            onExit = { mouseX, mouseY ->
//                searchIn.borderColor(Color(60, 60, 60).rgb)
//            },
//            onEnter = { mouseX, mouseY ->
//                searchIn.borderColor(Color(70, 120, 140).rgb)
//                searchIn.backgroundColor(Color(20, 20, 20).rgb)
//            }
//        )
//    }
//
//    override fun addConfig() {
//        ConfigManager
//            .addFeature("Inventory Search", "", "HUD", ConfigElement(
//                "inventorysearch",
//                ElementType.Switch(false)
//            ))
//            .addFeatureOption("Search lore", "", "Options", ConfigElement(
//                "inventorysearchlore",
//                ElementType.Switch(false)
//            ))
//            .addFeatureOption("Highlight color", "", "Options", ConfigElement(
//                "inventorysearchcolor",
//                ElementType.ColorPicker(Color(0, 127, 127, 127))
//            ))
//            .addFeatureOption("Highlight type", "", "Options", ConfigElement(
//                "inventorysearchtype",
//                ElementType.Dropdown(listOf("Slot", "Border"), 0)
//            ))
//    }
//
//
////    private fun calculateMath(input: String): String? {
////        return try {
////            val sanitized = input.replace(Regex("[^0-9+\\-*/().\\s]"), "")
////            if (sanitized.isBlank() || sanitized != input.trim()) return null
////            val result = CalculatorCommand.eval(sanitized)
////            if (result == result.toInt().toDouble()) result.toInt().toString()
////            else "%.1f".format(result).trimEnd('0').trimEnd('.')
////        } catch (_: Exception) {
////            null
////        }
////    }
//
//    override fun initialize() {
//        register<GuiEvent.AfterRender> { event ->
//            if (event.screen is HandledScreen<*>) {
//                searchIn.drawAsRoot()
//            }
//        }
//
//        register<GuiEvent.Slot.Render> { event ->
//            val text = searchIn.value.lowercase().removeFormatting().takeIf { it.isNotBlank() } ?: return@register
//            val item = event.slot.stack ?: return@register
//            if (item.item is AirBlockItem) return@register
//            val itemName = item.name.string.removeFormatting().trim().lowercase()
//            val searchableText =
//                if (searchLore) {
//                    (item.lore.map { it.removeFormatting().lowercase() } + itemName).joinToString(" ")
//                } else {
//                    itemName
//                }
//
//            if (!searchableText.contains(text)) return@register
//
//            val highlightColor = color.rgb
//            val x = event.slot.x.toFloat()
//            val y = event.slot.y.toFloat()
//            val matrices = event.context.matrices
//
//            NVGRenderer.push()
//            //#if MC >= 1.21.7
//            //$$ NVGRenderer.translate(matrices.m20(), matrices.m21())
//            //#else
//            NVGRenderer.translate(matrices.peek().positionMatrix.m30(), matrices.peek().positionMatrix.m31())
//            //#endif
//            when (highlightType) {
//                0 -> {
//                    NVGRenderer.globalAlpha(0.3f)
//                    NVGRenderer.rect(x, y, 16f, 16f, highlightColor)
//                }
//                1 -> NVGRenderer.hollowRect(x, y, 16f, 16f, 1f, highlightColor, 0f)
//            }
//
//            NVGRenderer.pop()
//        }
//    }
//}