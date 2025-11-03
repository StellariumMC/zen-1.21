package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.uuid
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.chestName
import net.minecraft.block.Blocks
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import com.mojang.serialization.Codec
import java.awt.Color

//#if MC >= 1.21.9
//$$ import net.minecraft.client.input.KeyInput
//$$ import net.minecraft.client.gui.Click
//#endif

/**
 * Module contains code from Skytils
 *
 * @license GPL-3.0
 */
@Module
object ProtectItem : Feature("protectitem", true) {
    val itemData = StoredFile("features/ProtectItem")
    var protectedItems: Set<String> by itemData.set("protectedItems", Codec.STRING)
    var protectedTypes: Set<String> by itemData.set("protectedTypes", Codec.STRING)

    override fun addConfig() {
        ConfigManager
            .addFeature("Item Protection", "", "General", ConfigElement(
                "protectitem",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Item Protection Info", "", "", ConfigElement(
                "",
                ElementType.TextParagraph("Tries to prevent you from dropping items that you have protected using §c/protectitem\n§7Aliases: /pitem, /zenpi")
            ))
            .addFeatureOption("Protect Item GUI", "Protect Item GUI", "GUI", ConfigElement(
                "protectItem.GuiButton",
                ElementType.Button("Open GUI") {
                    client.setScreen(ItemProtectGUI())
                }
            ))
    }

    override fun initialize() {
        register<EntityEvent.ItemToss> { event ->
            if (SkyBlockIsland.THE_CATACOMBS.inIsland()) return@register
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
                    client.interactionManager?.clickSlot(event.handler.syncId, slot.id, 0, SlotActionType.PICKUP, player)
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

                if (isSellGui && event.slotId != 49 && slot.inventory === player?.inventory) {
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
        if (itemUuid.isNotEmpty() && itemUuid in protectedItems) return true

        val itemId = item.item.toString()
        return itemId in protectedTypes
    }

    private fun sendProtectionMessage(action: String, itemName: String) {
        KnitChat.fakeMessage("$prefix §fStopped you from $action $itemName§r!")
    }
}

@Command
object ProtectItemCommand : Commodore("protectitem", "zenprotect", "pitem", "zenpi") {
    init {
        literal("gui") {
            runs {
                TickUtils.schedule(2) {
                    client.setScreen(ItemProtectGUI())
                }
            }
        }

        runs {
            val heldItem = player?.mainHandStack
            if (heldItem == null || heldItem.isEmpty) {
                KnitChat.fakeMessage("$prefix §cYou must be holding an item!")
                return@runs
            }
            val itemUuid = heldItem.uuid
            val itemId = heldItem.item.toString()
            if (itemUuid.isEmpty()) {
                if (itemId in ProtectItem.protectedTypes) {
                    ProtectItem.protectedTypes -= itemId
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fRemoved all ${heldItem.name.string} §ffrom protected items!")
                } else {
                    ProtectItem.protectedTypes += itemId
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fAdded all ${heldItem.name.string} §fto protected items! §7(No UUID - protecting by type)")
                }
            } else {
                if (itemUuid in ProtectItem.protectedItems) {
                    ProtectItem.protectedItems -= itemUuid
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fRemoved ${heldItem.name.string} §ffrom protected items!")
                } else {
                    ProtectItem.protectedItems += itemUuid
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fAdded ${heldItem.name.string} §fto protected items!")
                }
            }
        }
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
        val player = player ?: return
        val protectedSet = ProtectItem.protectedItems
        val protectedTypeSet = ProtectItem.protectedTypes

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

    //#if MC >= 1.21.9
    //$$ override fun mouseClicked(click: Click?, doubled: Boolean): Boolean {
    //$$     if (click?.keycode == 0) {
    //$$         slots.forEachIndexed { _, slot ->
    //$$             if (click.x >= slot.x && click.x <= slot.x + slotSize && click.y >= slot.y && click.y <= slot.y + slotSize) {
    //$$                 if (!slot.stack.isEmpty) toggleProtection(slot)
    //$$                 return true
    //$$              }
    //$$         }
    //$$     }
    //$$
    //$$     return super.mouseClicked(click, doubled)
    //$$ }
    //#else
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
    //#endif

    //#if MC >= 1.21.9
    //$$ override fun keyPressed(input: KeyInput?): Boolean {
    //$$    when (input?.keycode) {
    //#else
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
    //#endif
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

        //#if MC >= 1.21.9
        //$$ return super.keyPressed(input)
        //#else
        return super.keyPressed(keyCode, scanCode, modifiers)
        //#endif
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