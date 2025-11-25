package xyz.meowing.zen.features.general.protectItem

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.uuid
import xyz.meowing.zen.utils.Utils.chestName
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import com.mojang.serialization.Codec
import net.minecraft.network.chat.Component
import xyz.meowing.zen.utils.Utils.toLegacyString

/**
 * Module contains code from Skytils
 *
 * @license GPL-3.0
 */
@Module
object ProtectItem : Feature(
    "protectItem",
    true
) {
    val itemData = StoredFile("features/ProtectItem")
    var protectedItems: Set<String> by itemData.set("protectedItems", Codec.STRING)
    var protectedTypes: Set<String> by itemData.set("protectedTypes", Codec.STRING)

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Item protection",
                "Tries to prevent you from dropping items that you have protected using §c/protectitem\n§7Aliases: /pitem, /zenpi",
                "General",
                ConfigElement(
                    "protectItem",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Protect item GUI",
                ConfigElement(
                    "protectItem.guiButton",
                    ElementType.Button("Open GUI") {
                        client.setScreen(ProtectItemGUI())
                    }
                )
            )
    }

    override fun initialize() {
        register<EntityEvent.ItemToss> { event ->
            if (SkyBlockIsland.THE_CATACOMBS.inIsland()) return@register
            if (isProtected(event.stack)) {
                sendProtectionMessage("dropping", event.stack.hoverName)
                event.cancel()
            }
        }

        register<GuiEvent.Close> { event ->
            val item = event.handler.carried ?: return@register
            if (isProtected(item)) {
                for (slot in event.handler.slots) {
                    if (slot.container !== player?.inventory || slot.hasItem() || !slot.mayPlace(item)) continue
                    client.gameMode?.handleInventoryMouseClick(event.handler.containerId, slot.index, 0, ClickType.PICKUP, player)
                    sendProtectionMessage("dropping", item.hoverName)
                    event.cancel()
                    return@register
                }
            }
        }

        register<GuiEvent.Slot.Click> { event ->
            if (event.handler is ChestMenu) handleChestClick(event)
            handleDropClick(event)
        }
    }

    private fun handleChestClick(event: GuiEvent.Slot.Click) {
        val handler = event.handler as ChestMenu
        val inv = handler.container
        val chestName = event.screen.chestName
        val slot = event.slot ?: return
        val item = slot.item ?: return

        if (!slot.hasItem() || !isProtected(item)) return

        when {
            chestName.startsWith("Salvage") -> {
                val inSalvageGui = item.hoverName.string.contains("Salvage") || item.hoverName.string.contains("Essence")
                if (inSalvageGui || slot.container === player?.inventory) {
                    sendProtectionMessage("salvaging", item.hoverName)
                    event.cancel()
                }
            }

            chestName != "Large Chest" && inv.containerSize == 54 && !chestName.contains("Auction") -> {
                val sellItem = inv.getItem(49)
                val isSellGui = sellItem?.item === Blocks.HOPPER.asItem() && (sellItem.hoverName.string.contains("Sell Item") || sellItem.lore.any { it.contains("buyback") })

                if (isSellGui && event.slotId != 49 && slot.container === player?.inventory) {
                    sendProtectionMessage("selling", item.hoverName)
                    event.cancel()
                }
            }

            chestName.startsWith("Create ") && chestName.endsWith(" Auction") -> {
                if (inv.getItem(13) != null) {
                    sendProtectionMessage("auctioning", item.hoverName)
                    event.cancel()
                }
            }
        }
    }

    private fun handleDropClick(event: GuiEvent.Slot.Click) {
        val item = when {
            event.slotId == -999 && event.handler.carried != null && event.button != 5 -> event.handler.carried
            event.button == 4 && event.slotId != -999 && event.slot?.hasItem() == true -> event.slot.item
            else -> return
        } ?: return

        if (isProtected(item)) {
            sendProtectionMessage("dropping", item.hoverName)
            event.cancel()
        }
    }

    private fun isProtected(item: ItemStack): Boolean {
        val itemUuid = item.uuid
        if (itemUuid.isNotEmpty() && itemUuid in protectedItems) return true

        val itemId = item.item.toString()
        return itemId in protectedTypes
    }

    private fun sendProtectionMessage(action: String, itemName: Component) {
        KnitChat.fakeMessage("$prefix §fStopped you from $action ${itemName.toLegacyString()}§r!")
    }
}