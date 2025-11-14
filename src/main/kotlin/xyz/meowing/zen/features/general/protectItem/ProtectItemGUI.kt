package xyz.meowing.zen.features.general.protectItem

import xyz.meowing.zen.utils.ItemUtils.uuid
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.screen.KnitScreen
import java.awt.Color

//#if MC >= 1.21.9
//$$ import xyz.meowing.zen.utils.Render2D.renderOutline
//#endif

class ProtectItemGUI : KnitScreen("Protect Item GUI") {
    private val slots = mutableListOf<InventorySlot>()
    private var hoveredSlot = -1
    private val slotSize = 18

    private val guiWidth = 194
    private val guiHeight = 160

    private val titleColor = Color(180, 220, 255).rgb
    private val instructionColor = Color(200, 200, 200).rgb
    private val backgroundBlur = Color(0, 0, 0, 120).rgb
    private val guiBackground = Color(40, 40, 50, 200).rgb
    private val guiBorder = Color(120, 140, 160, 255).rgb
    private val protectedSlotColor = Color(40, 120, 40, 180).rgb
    private val hoveredSlotColor = Color(80, 80, 100, 180).rgb
    private val normalSlotColor = Color(60, 60, 70, 180).rgb
    private val protectedBorder = Color(80, 200, 80, 255).rgb
    private val typeProtectedColor = Color(120, 100, 40, 180).rgb
    private val typeProtectedBorder = Color(200, 160, 80, 255).rgb

    data class InventorySlot(
        val stack: ItemStack,
        val uuid: String,
        val itemId: String,
        var isProtected: Boolean,
        var isTypeProtected: Boolean,
        val x: Int,
        val y: Int,
        val slotIndex: Int
    )

    override fun onInitGui() {
        loadInventory()
    }

    private fun loadInventory() {
        slots.clear()
        val player = player ?: return
        val protectedSet = ProtectItem.protectedItems
        val protectedTypeSet = ProtectItem.protectedTypes

        val guiX = (width - guiWidth) / 2
        val guiY = (height - guiHeight) / 2

        val armorSlots = listOf(39, 38, 37, 36)

        armorSlots.forEachIndexed { index, slotIndex ->
            val stack = player.inventory.getItem(slotIndex)
            val uuid = if (!stack.isEmpty) stack.uuid else ""
            val itemId = if (!stack.isEmpty) stack.item.toString() else ""

            val x = guiX + 8 + (index * 20)
            val y = guiY + 30

            slots.add(InventorySlot(
                stack, uuid, itemId,
                uuid.isNotEmpty() && uuid in protectedSet,
                itemId.isNotEmpty() && itemId in protectedTypeSet,
                x, y, slotIndex
            ))
        }

        for (row in 0..2) {
            for (col in 0..8) {
                val slotIndex = 9 + row * 9 + col
                val stack = player.inventory.getItem(slotIndex)
                val uuid = if (!stack.isEmpty) stack.uuid else ""
                val itemId = if (!stack.isEmpty) stack.item.toString() else ""

                val x = guiX + 8 + (col * 20)
                val y = guiY + 60 + (row * 20)

                slots.add(InventorySlot(
                    stack, uuid, itemId,
                    uuid.isNotEmpty() && uuid in protectedSet,
                    itemId.isNotEmpty() && itemId in protectedTypeSet,
                    x, y, slotIndex
                ))
            }
        }

        for (col in 0..8) {
            val stack = player.inventory.getItem(col)
            val uuid = if (!stack.isEmpty) stack.uuid else ""
            val itemId = if (!stack.isEmpty) stack.item.toString() else ""

            val x = guiX + 8 + (col * 20)
            val y = guiY + 130

            slots.add(InventorySlot(
                stack, uuid, itemId,
                uuid.isNotEmpty() && uuid in protectedSet,
                itemId.isNotEmpty() && itemId in protectedTypeSet,
                x, y, col
            ))
        }
    }

    override fun onRender(context: GuiGraphics?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val context = context ?: return // never null anyway, TODO: fix in knit
        renderBackground(context, mouseX, mouseY, deltaTicks)

        val guiX = (width - guiWidth) / 2
        val guiY = (height - guiHeight) / 2

        context.fill(guiX, guiY, guiX + guiWidth, guiY + guiHeight, guiBackground)

        context.renderOutline(guiX, guiY, guiX + guiWidth, guiY + guiHeight, guiBorder)

        val title = "Item Protection Manager"
        val titleWidth = font.width(title)
        val titleX = guiX + (guiWidth - titleWidth) / 2
        Render2D.renderString(context, title, titleX.toFloat(), guiY + 8f, 1f, titleColor, Render2D.TextStyle.DROP_SHADOW)

        hoveredSlot = -1
        slots.forEachIndexed { index, slot ->
            renderSlot(context, slot, mouseX, mouseY, index)
        }

        if (hoveredSlot >= 0) renderTooltip(context, slots[hoveredSlot], mouseX, mouseY)

        val instructions = "L to toggle protection • ESC to close"
        val textWidth = font.width(instructions)
        Render2D.renderString(context, instructions, guiX + (guiWidth - textWidth) / 2f, guiY + guiHeight + 5f, 1f, instructionColor, Render2D.TextStyle.DROP_SHADOW)

        super.render(context, mouseX, mouseY, deltaTicks)
    }

    private fun renderSlot(context: GuiGraphics, slot: InventorySlot, mouseX: Int, mouseY: Int, index: Int) {
        val isHovered = mouseX >= slot.x && mouseX <= slot.x + slotSize && mouseY >= slot.y && mouseY <= slot.y + slotSize

        if (isHovered) hoveredSlot = index

        val slotColor = when {
            slot.isProtected -> protectedSlotColor
            slot.isTypeProtected -> typeProtectedColor
            isHovered -> hoveredSlotColor
            else -> normalSlotColor
        }

        context.fill(slot.x, slot.y, slot.x + slotSize, slot.y + slotSize, slotColor)

        val borderColor = when {
            slot.isProtected -> protectedBorder
            slot.isTypeProtected -> typeProtectedBorder
            else -> guiBorder
        }

        context.renderOutline(slot.x, slot.y, slot.x + slotSize, slot.y + slotSize, borderColor)

        if (!slot.stack.isEmpty) {
            context.renderItem(slot.stack, slot.x + 1, slot.y + 1)
            context.renderItemDecorations(font, slot.stack, slot.x + 1, slot.y + 1)
        }
    }

    private fun renderTooltip(context: GuiGraphics, slot: InventorySlot, mouseX: Int, mouseY: Int) {
        val lines = mutableListOf<Component>()

        when {
            slot.stack.isEmpty -> lines.add(Component.literal("§7Empty Slot"))
            else -> {
                lines.add(slot.stack.hoverName)

                when {
                    slot.isProtected -> lines.add(Component.literal("§aProtected (UUID) - Press L to unprotect"))
                    slot.isTypeProtected -> lines.add(Component.literal("§6Protected (All of type) - Press L to unprotect"))
                    slot.uuid.isNotEmpty() -> lines.add(Component.literal("§7Not protected - Press L to protect"))
                    else -> lines.add(Component.literal("§7No UUID - Press L to protect all of this type"))
                }
            }
        }

        context.renderComponentTooltip(font, lines, mouseX, mouseY)
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        if (button == 0) {
            slots.forEachIndexed { _, slot ->
                if (mouseX >= slot.x && mouseX <= slot.x + slotSize && mouseY >= slot.y && mouseY <= slot.y + slotSize) {
                    if (!slot.stack.isEmpty) toggleProtection(slot)
                }
            }
        }
    }

    override fun onKeyType(typedChar: Char, keyCode: Int, scanCode: Int) {
        super.onKeyType(typedChar, keyCode, scanCode)
        when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE -> {
                onClose()
            }
            GLFW.GLFW_KEY_L -> {
                if (hoveredSlot >= 0) {
                    val slot = slots[hoveredSlot]
                    if (!slot.stack.isEmpty) toggleProtection(slot)
                }
            }
        }
    }

    private fun toggleProtection(slot: InventorySlot) {
        if (slot.uuid.isNotEmpty()) {
            if (slot.uuid in ProtectItem.protectedItems) {
                ProtectItem.protectedItems -= slot.uuid
                slot.isProtected = false
            } else {
                ProtectItem.protectedItems += slot.uuid
                slot.isProtected = true
            }
            ProtectItem.itemData.forceSave()
        } else {
            if (slot.itemId in ProtectItem.protectedTypes) {
                ProtectItem.protectedTypes -= slot.itemId
                slots.forEach { s ->
                    if (s.itemId == slot.itemId) {
                        s.isTypeProtected = false
                    }
                }
            } else {
                ProtectItem.protectedTypes += slot.itemId
                slots.forEach { s ->
                    if (s.itemId == slot.itemId) {
                        s.isTypeProtected = true
                    }
                }
            }
            ProtectItem.itemData.forceSave()
        }
    }

    override fun isPauseScreen() = false

    override fun renderBackground(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, backgroundBlur)
    }
}