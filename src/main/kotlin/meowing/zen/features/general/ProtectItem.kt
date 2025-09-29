package meowing.zen.features.general

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.ItemUtils.uuid
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.Render2D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.chestName
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.block.Blocks
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.awt.Color

/**
 * Module contains code from Skytils
 *
 * @license GPL-3.0
 */
@Zen.Module
object ProtectItem : Feature("protectitem", true) {
    val protectedItems = DataUtils("protected_items", mutableSetOf<String>())
    val protectedTypes = DataUtils("protected_types", mutableSetOf<String>())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Item Protection", ConfigElement(
                "protectitem",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Item Protection", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Tries to prevent you from dropping items that you have protected using §c/protectitem\n§7Aliases: /pitem, /zenpi")
            ))
            .addElement("General", "Item Protection", "GUI", ConfigElement(
                "protectItem.GuiButton",
                "Protect Item GUI",
                ElementType.Button("Open GUI") {
                    mc.setScreen(ItemProtectGUI())
                }
            ))
    }

    override fun initialize() {
        register<EntityEvent.ItemToss> { event ->
            if (LocationUtils.checkArea("catacombs")) return@register
            if (isProtected(event.stack)) {
                sendProtectionMessage("dropping", event.stack.name.string)
                event.cancel()
            }
        }

        register<GuiEvent.Close> { event ->
            val item = event.handler.cursorStack ?: return@register
            if (isProtected(item)) {
                for (slot in event.handler.slots) {
                    if (slot.inventory !== player?.inventory || slot.hasStack() || !slot.canInsert(item)) continue
                    mc.interactionManager?.clickSlot(event.handler.syncId, slot.id, 0, SlotActionType.PICKUP, player)
                    sendProtectionMessage("dropping", item.name.string)
                    event.cancel()
                    return@register
                }
            }
        }

        register<GuiEvent.Slot.Click> { event ->
            if (event.handler is GenericContainerScreenHandler) handleChestClick(event)
            handleDropClick(event)
        }
    }

    private fun handleChestClick(event: GuiEvent.Slot.Click) {
        val handler = event.handler as GenericContainerScreenHandler
        val inv = handler.inventory
        val chestName = event.screen.chestName
        val slot = event.slot ?: return
        val item = slot.stack ?: return

        if (!slot.hasStack() || !isProtected(item)) return

        when {
            chestName.startsWith("Salvage") -> {
                val inSalvageGui = item.name.string.contains("Salvage") || item.name.string.contains("Essence")
                if (inSalvageGui || slot.inventory === player?.inventory) {
                    sendProtectionMessage("salvaging", item.name.string)
                    event.cancel()
                }
            }

            chestName != "Large Chest" && inv.size() == 54 && !chestName.contains("Auction") -> {
                val sellItem = inv.getStack(49)
                val isSellGui = sellItem?.item === Blocks.HOPPER.asItem() && (sellItem.name.string.contains("Sell Item") || sellItem.lore.any { it.contains("buyback") })

                if (isSellGui && event.slotId != 49 && slot.inventory === mc.player?.inventory) {
                    sendProtectionMessage("selling", item.name.string)
                    event.cancel()
                }
            }

            chestName.startsWith("Create ") && chestName.endsWith(" Auction") -> {
                if (inv.getStack(13) != null) {
                    sendProtectionMessage("auctioning", item.name.string)
                    event.cancel()
                }
            }
        }
    }

    private fun handleDropClick(event: GuiEvent.Slot.Click) {
        val item = when {
            event.slotId == -999 && event.handler.cursorStack != null && event.button != 5 -> event.handler.cursorStack
            event.button == 4 && event.slotId != -999 && event.slot?.hasStack() == true -> event.slot.stack
            else -> return
        } ?: return

        if (isProtected(item)) {
            sendProtectionMessage("dropping", item.name.string)
            event.cancel()
        }
    }

    private fun isProtected(item: ItemStack): Boolean {
        val itemUuid = item.uuid
        if (itemUuid.isNotEmpty() && itemUuid in protectedItems()) return true

        val itemId = item.item.toString()
        return itemId in protectedTypes()
    }

    private fun sendProtectionMessage(action: String, itemName: String) {
        ChatUtils.addMessage("$prefix §fStopped you from $action $itemName§r!")
    }
}

@Zen.Command
object ProtectItemCommand : CommandUtils("protectitem", aliases = listOf("zenprotect", "pitem", "zenpi")) {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val heldItem = mc.player?.mainHandStack
        if (heldItem == null || heldItem.isEmpty) {
            ChatUtils.addMessage("$prefix §cYou must be holding an item!")
            return 0
        }

        val itemUuid = heldItem.uuid
        val itemId = heldItem.item.toString()

        if (itemUuid.isEmpty()) {
            ProtectItem.protectedTypes.update {
                if (itemId in this) {
                    remove(itemId)
                    ChatUtils.addMessage("$prefix §fRemoved all ${heldItem.name.string} §ffrom protected items!")
                } else {
                    add(itemId)
                    ChatUtils.addMessage("$prefix §fAdded all ${heldItem.name.string} §fto protected items! §7(No UUID - protecting by type)")
                }
            }
        } else {
            ProtectItem.protectedItems.update {
                if (itemUuid in this) {
                    remove(itemUuid)
                    ChatUtils.addMessage("$prefix §fRemoved ${heldItem.name.string} §ffrom protected items!")
                } else {
                    add(itemUuid)
                    ChatUtils.addMessage("$prefix §fAdded ${heldItem.name.string} §fto protected items!")
                }
            }
        }

        return 1
    }

    override fun buildCommand(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            ClientCommandManager.literal("gui")
                .executes {
                    TickUtils.schedule(2) {
                        mc.setScreen(ItemProtectGUI())
                    }
                    1
                }
        )
    }
}

class ItemProtectGUI : Screen(Text.literal("Item Protection")) {
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

    override fun init() {
        super.init()
        loadInventory()
    }

    private fun loadInventory() {
        slots.clear()
        val player = mc.player ?: return
        val protectedSet = ProtectItem.protectedItems()
        val protectedTypeSet = ProtectItem.protectedTypes()

        val guiX = (width - guiWidth) / 2
        val guiY = (height - guiHeight) / 2

        val armorSlots = listOf(39, 38, 37, 36)

        armorSlots.forEachIndexed { index, slotIndex ->
            val stack = player.inventory.getStack(slotIndex)
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
                val stack = player.inventory.getStack(slotIndex)
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
            val stack = player.inventory.getStack(col)
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

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)

        val guiX = (width - guiWidth) / 2
        val guiY = (height - guiHeight) / 2

        context.fill(guiX, guiY, guiX + guiWidth, guiY + guiHeight, guiBackground)

        drawHollowRect(context, guiX, guiY, guiX + guiWidth, guiY + guiHeight, guiBorder)

        val title = "Item Protection Manager"
        val titleWidth = textRenderer.getWidth(title)
        val titleX = guiX + (guiWidth - titleWidth) / 2
        Render2D.renderString(context, title, titleX.toFloat(), guiY + 8f, 1f, titleColor, Render2D.TextStyle.DROP_SHADOW)

        hoveredSlot = -1
        slots.forEachIndexed { index, slot ->
            renderSlot(context, slot, mouseX, mouseY, index)
        }

        if (hoveredSlot >= 0) renderTooltip(context, slots[hoveredSlot], mouseX, mouseY)

        val instructions = "L to toggle protection • ESC to close"
        val textWidth = textRenderer.getWidth(instructions)
        Render2D.renderString(context, instructions, guiX + (guiWidth - textWidth) / 2f, guiY + guiHeight + 5f, 1f, instructionColor, Render2D.TextStyle.DROP_SHADOW)

        super.render(context, mouseX, mouseY, delta)
    }

    private fun renderSlot(context: DrawContext, slot: InventorySlot, mouseX: Int, mouseY: Int, index: Int) {
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

        drawHollowRect(context, slot.x, slot.y, slot.x + slotSize, slot.y + slotSize, borderColor)

        if (!slot.stack.isEmpty) {
            context.drawItem(slot.stack, slot.x + 1, slot.y + 1)
            context.drawStackOverlay(textRenderer, slot.stack, slot.x + 1, slot.y + 1)
        }
    }

    private fun renderTooltip(context: DrawContext, slot: InventorySlot, mouseX: Int, mouseY: Int) {
        val lines = mutableListOf<Text>()

        when {
            slot.stack.isEmpty -> lines.add(Text.literal("§7Empty Slot"))
            else -> {
                lines.add(slot.stack.name)

                when {
                    slot.isProtected -> lines.add(Text.literal("§aProtected (UUID) - Press L to unprotect"))
                    slot.isTypeProtected -> lines.add(Text.literal("§6Protected (All of type) - Press L to unprotect"))
                    slot.uuid.isNotEmpty() -> lines.add(Text.literal("§7Not protected - Press L to protect"))
                    else -> lines.add(Text.literal("§7No UUID - Press L to protect all of this type"))
                }
            }
        }

        context.drawTooltip(textRenderer, lines, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            slots.forEachIndexed { _, slot ->
                if (mouseX >= slot.x && mouseX <= slot.x + slotSize && mouseY >= slot.y && mouseY <= slot.y + slotSize) {
                    if (!slot.stack.isEmpty) toggleProtection(slot)
                    return true
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE -> {
                close()
                return true
            }
            GLFW.GLFW_KEY_L -> {
                if (hoveredSlot >= 0) {
                    val slot = slots[hoveredSlot]
                    if (!slot.stack.isEmpty) toggleProtection(slot)
                    return true
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun toggleProtection(slot: InventorySlot) {
        if (slot.uuid.isNotEmpty()) {
            ProtectItem.protectedItems.update {
                if (slot.uuid in this) {
                    remove(slot.uuid)
                    slot.isProtected = false
                } else {
                    add(slot.uuid)
                    slot.isProtected = true
                }
            }
        } else {
            ProtectItem.protectedTypes.update {
                if (slot.itemId in this) {
                    remove(slot.itemId)
                    slots.forEach { s ->
                        if (s.itemId == slot.itemId) {
                            s.isTypeProtected = false
                        }
                    }
                } else {
                    add(slot.itemId)
                    slots.forEach { s ->
                        if (s.itemId == slot.itemId) {
                            s.isTypeProtected = true
                        }
                    }
                }
            }
        }
    }

    private fun drawHollowRect(context: DrawContext, x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        context.fill(x1, y1, x2, y1 + 1, color)
        context.fill(x1, y2 - 1, x2, y2, color)
        context.fill(x1, y1, x1 + 1, y2, color)
        context.fill(x2 - 1, y1, x2, y2, color)
    }

    override fun shouldPause() = false

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, backgroundBlur)
    }
}