package xyz.meowing.zen.features.general.protectItem

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.utils.ItemUtils.uuid
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Command

@Command
object ProtectItemCommand : Commodore("protectitem", "zenprotect", "pitem", "zenpi") {
    init {
        literal("gui") {
            runs {
                TickScheduler.Client.schedule(2) {
                    client.setScreen(ProtectItemGUI())
                }
            }
        }

        runs {
            val heldItem = player?.mainHandItem
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
                    KnitChat.fakeMessage("$prefix §fRemoved all ${heldItem.hoverName.string} §ffrom protected items!")
                } else {
                    ProtectItem.protectedTypes += itemId
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fAdded all ${heldItem.hoverName.string} §fto protected items! §7(No UUID - protecting by type)")
                }
            } else {
                if (itemUuid in ProtectItem.protectedItems) {
                    ProtectItem.protectedItems -= itemUuid
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fRemoved ${heldItem.hoverName.string} §ffrom protected items!")
                } else {
                    ProtectItem.protectedItems += itemUuid
                    ProtectItem.itemData.forceSave()
                    KnitChat.fakeMessage("$prefix §fAdded ${heldItem.hoverName.string} §fto protected items!")
                }
            }
        }
    }
}