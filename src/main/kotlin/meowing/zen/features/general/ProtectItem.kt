package meowing.zen.features.general

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
import meowing.zen.utils.Utils.chestName
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.block.Blocks
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.SlotActionType

/**
 * Module contains code from Skytils
 *
 * @license GPL-3.0
 */
@Zen.Module
object ProtectItem : Feature("protectitem", true) {
    val protectedItems = DataUtils("protected_items", mutableSetOf<String>())

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
    }

    override fun initialize() {
        register<EntityEvent.ItemToss> { event ->
            val itemUuid = event.stack.uuid
            if (itemUuid.isNotEmpty() && itemUuid in protectedItems()) {
                sendProtectionMessage("dropping", event.stack.name.string)
                event.cancel()
            }
        }

        register<GuiEvent.Close> { event ->
            val item = event.handler.cursorStack ?: return@register
            val itemUuid = item.uuid
            if (itemUuid.isNotEmpty() && itemUuid in protectedItems()) {
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
        val itemUuid = item.uuid

        if (!slot.hasStack() || itemUuid.isEmpty() || itemUuid !in protectedItems()) return

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

        val itemUUID = item.uuid
        if (itemUUID.isNotEmpty() && itemUUID in protectedItems()) {
            sendProtectionMessage("dropping", item.name.string)
            event.cancel()
        }
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
        if (itemUuid.isEmpty()) {
            ChatUtils.addMessage("$prefix §cThis item doesn't have a UUID!")
            return 0
        }

        ProtectItem.protectedItems.update {
            if (itemUuid in this) {
                remove(itemUuid)
                ChatUtils.addMessage("$prefix §fRemoved ${heldItem.name.string} §ffrom protected items!")
            } else {
                add(itemUuid)
                ChatUtils.addMessage("$prefix §fAdded ${heldItem.name.string} §fto protected items!")
            }
        }

        return 1
    }
}