package xyz.meowing.zen.config.ui.elements

import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.ui.Theme
import java.awt.Color

enum class MCColorCode(val code: String, val color: Color, val displayName: String) {
    BLACK("§0", Color(0, 0, 0), "Black"),
    DARK_BLUE("§1", Color(0, 0, 170), "Dark Blue"),
    DARK_GREEN("§2", Color(0, 170, 0), "Dark Green"),
    DARK_AQUA("§3", Color(0, 170, 170), "Dark Aqua"),
    DARK_RED("§4", Color(170, 0, 0), "Dark Red"),
    DARK_PURPLE("§5", Color(170, 0, 170), "Dark Purple"),
    GOLD("§6", Color(255, 170, 0), "Gold"),
    GRAY("§7", Color(170, 170, 170), "Gray"),
    DARK_GRAY("§8", Color(85, 85, 85), "Dark Gray"),
    BLUE("§9", Color(85, 85, 255), "Blue"),
    GREEN("§a", Color(85, 255, 85), "Green"),
    AQUA("§b", Color(85, 255, 255), "Aqua"),
    RED("§c", Color(255, 85, 85), "Red"),
    LIGHT_PURPLE("§d", Color(255, 85, 255), "Light Purple"),
    YELLOW("§e", Color(255, 255, 85), "Yellow"),
    WHITE("§f", Color(255, 255, 255), "White")
}

class MCColorPickerElement(
    name: String,
    private var selectedColor: MCColorCode
) : VexelElement<MCColorPickerElement>() {

    private val colorBoxes = mutableListOf<Rectangle>()

    private val label = Text(name, Theme.Text.color, 16f)
        .setPositioning(6f, Pos.ParentPixels, 6f, Pos.ParentPixels)
        .childOf(this)

    init {
        setSizing(240f, Size.Pixels, 48f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.AfterSibling)

        createColorGrid()
    }

    private fun createColorGrid() {
        val colors = MCColorCode.entries

        colors.forEachIndexed { index, mcColor ->
            val colorBox = Rectangle(
                mcColor.color.rgb,
                if (mcColor == selectedColor) Theme.Border.color else 0x00000000,
                2f,
                if (mcColor == selectedColor) 2f else 0f
            )
                .setSizing(13f, Size.Pixels, 13f, Size.Pixels)
                .setPositioning(
                    6f + index * 14f,
                    Pos.ParentPixels,
                    26f,
                    Pos.ParentPixels
                )
                .childOf(this)

            colorBox.onClick { _, _, _ ->
                selectColor(mcColor)
                true
            }

            colorBoxes.add(colorBox)
        }
    }

    private fun selectColor(mcColor: MCColorCode) {
        selectedColor = mcColor
        updateSelection()
        onValueChange.forEach { it.invoke(selectedColor) }
    }

    private fun updateSelection() {
        val colors = MCColorCode.entries

        colorBoxes.forEachIndexed { index, box ->
            if (colors[index] == selectedColor) {
                box.borderThickness = 2f
                box.borderColor = Theme.Border.color
            } else {
                box.borderThickness = 0f
                box.borderColor = 0x00000000
            }
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}