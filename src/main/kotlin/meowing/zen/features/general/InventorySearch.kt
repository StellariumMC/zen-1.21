package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.ui.components.TextInputComponent
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.AirBlockItem
import org.lwjgl.glfw.GLFW
import java.awt.Color

@Zen.Module
object InventorySearch : Feature() {
    private val searchLore by ConfigDelegate<Boolean>("inventorysearchlore")
    private val highlightType by ConfigDelegate<Int>("inventorysearchtype")
    private val color by ConfigDelegate<Color>("inventorysearchcolor")
    private var mathResult: String? = null
    private val searchInput = TextInputComponent(
        placeholder = "Search...",
        x = 0,
        y = 0,
        width = 200,
        height = 25,
        accentColor = Color(170, 230, 240),
        hoverColor = Color(70, 120, 140)
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Inventory Search", ConfigElement(
                "inventorysearch",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Inventory Search", "Options", ConfigElement(
                "inventorysearchlore",
                "Search lore",
                ElementType.Switch(false)
            ))
            .addElement("General", "Inventory Search", "Options", ConfigElement(
                "inventorysearchcolor",
                "Highlight color",
                ElementType.ColorPicker(Color(0, 127, 127, 127))
            ))
            .addElement("General", "Inventory Search", "Options", ConfigElement(
                "inventorysearchtype",
                "Highlight type",
                ElementType.Dropdown(listOf("Slot", "Border"), 0)
            ))
    }

    private fun calculateMath(input: String): String? {
        return try {
            val sanitized = input.replace(Regex("[^0-9+\\-*/().\\s]"), "")
            if (sanitized.isBlank() || sanitized != input.trim()) return null
            val result = CalculatorCommand.eval(sanitized)
            if (result == result.toInt().toDouble()) result.toInt().toString()
            else "%.1f".format(result).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            null
        }
    }

    override fun initialize() {
        register<GuiEvent.AfterRender> { event ->
            if (event.screen is HandledScreen<*>) {
                searchInput.run {
                    val sf = (2 / window.scaleFactor).toFloat()
                    val screenWidth = window.scaledWidth / sf
                    val screenHeight = window.scaledHeight / sf
                    x = ((screenWidth - width) / 2).toInt()
                    y = (screenHeight * 0.95 - height / 2).toInt()

                    val matrices = event.context.matrices

                    //#if MC >= 1.21.7
                    //$$ matrices.pushMatrix()
                    //$$ matrices.scale(sf, sf)
                    //#else
                    matrices.push()
                    matrices.scale(sf, sf, sf)
                    matrices.translate(0f, 0f, 300f)
                    //#endif

                    draw(event.context, mouseX.toInt(), mouseY.toInt())

                    mathResult?.let { result ->
                        if (focused && value.isNotEmpty()) {
                            val textEndX = (x + textPadding - scrollOffset + mc.textRenderer.getWidth(value)).toFloat()
                            val textY = (y + (height - mc.textRenderer.fontHeight - 0.5) / 2).toFloat()
                            Render2D.renderString(event.context, " = $result", textEndX, textY, 1f, 0x55FF55)
                        }
                    }

                    //#if MC >= 1.21.7
                    //$$ matrices.popMatrix()
                    //#else
                    matrices.pop()
                    //#endif
                }
            }
        }

        register<GuiEvent.Click> { event ->
            if (event.screen is HandledScreen<*>) {
                searchInput.mouseClicked(mouseX.toInt(), mouseY.toInt(), event.mbtn)
            }
        }

        register<GuiEvent.Key> { event ->
            if (event.screen !is HandledScreen<*>) return@register

            if (Screen.hasControlDown() && event.key == GLFW.GLFW_KEY_F) {
                searchInput.focused = !searchInput.focused
                event.cancel()
                return@register
            }

            val keyHandled = searchInput.keyTyped(event.key)
            var charHandled = false

            if (searchInput.focused && event.key != GLFW.GLFW_KEY_UNKNOWN) {
                val window = mc.window.handle
                val shiftPressed =
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS

                val char = when (event.key) {
                    GLFW.GLFW_KEY_EQUAL -> if (shiftPressed) '+' else '='
                    GLFW.GLFW_KEY_MINUS -> if (shiftPressed) '_' else '-'
                    GLFW.GLFW_KEY_0 -> if (shiftPressed) ')' else '0'
                    GLFW.GLFW_KEY_1 -> if (shiftPressed) '!' else '1'
                    GLFW.GLFW_KEY_5 -> if (shiftPressed) '%' else '5'
                    GLFW.GLFW_KEY_6 -> if (shiftPressed) '^' else '6'
                    GLFW.GLFW_KEY_8 -> if (shiftPressed) '*' else '8'
                    GLFW.GLFW_KEY_9 -> if (shiftPressed) '(' else '9'
                    GLFW.GLFW_KEY_SLASH -> if (shiftPressed) '?' else '/'
                    GLFW.GLFW_KEY_PERIOD -> if (shiftPressed) '>' else '.'
                    GLFW.GLFW_KEY_SPACE -> ' '
                    in GLFW.GLFW_KEY_1..GLFW.GLFW_KEY_9 -> (event.key - GLFW.GLFW_KEY_0 + '0'.code).toChar()
                    else -> event.keyName?.firstOrNull()
                }

                char?.let {
                    charHandled = searchInput.charTyped(it)
                }
            }

            if (keyHandled || charHandled) {
                mathResult = if (searchInput.value.isNotEmpty()) calculateMath(searchInput.value) else null
                event.cancel()
            }
        }

        register<GuiEvent.Slot.Render> { event ->
            val text = searchInput.value.lowercase().removeFormatting().takeIf { it.isNotBlank() } ?: return@register
            val item = event.slot.stack ?: return@register
            if (item.item is AirBlockItem) return@register
            val itemName = item.name.string.removeFormatting().trim().lowercase()
            val searchableText =
                if (searchLore) {
                    (item.lore.map { it.removeFormatting().lowercase() } + itemName).joinToString(" ")
                } else {
                    itemName
                }

            if (!searchableText.contains(text)) return@register

            val highlightColor = color.rgb
            val x = event.slot.x
            val y = event.slot.y
            val context = event.context

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