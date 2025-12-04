package xyz.meowing.zen.api.skyblock

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.AbstractContainerMenu
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.chestName
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.Utils.toLegacyString

@Module
object TradeAPI {
    val tradeData = StoredFile("api/TradeAPI")
    var tradeHistory: JsonObject by tradeData.jsonObject("tradeHistory")

    private var inTradeMenu = false
    private var lastTradeMenu: AbstractContainerMenu? = null
    private var tradingWith = ""
    private var tradingWithSub = ""

    private val yourSlots = listOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30)
    private val theirSlots = listOf(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35)

    init {
        TickScheduler.Client.repeat(20) {
            if (KnitClient.client.screen == null) inTradeMenu = false
            val world = KnitClient.world ?: return@repeat

            if (tradingWithSub.isNotEmpty()) {
                world.players().find { it.name.string.contains(tradingWithSub) }?.let {
                    tradingWith = it.name.string
                }
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            if (event.message.string.removeFormatting().startsWith("Trade completed with")) {
                interpretLastTradeMenu()
                inTradeMenu = false
            }
        }

        EventBus.register<GuiEvent.Slot.Render> { event ->
            if (event.slot.index == 0) {
                inTradeMenu = false
                val tradeSlot = event.screen.menu.slots?.get(4) ?: return@register
                if (tradeSlot.item?.hoverName?.string?.removeFormatting() != "⇦ Your stuff") return@register

                inTradeMenu = true
                lastTradeMenu = event.screen.menu
                tradingWithSub = event.screen.chestName.split("You")[1].trim()
            }
        }
    }

    private fun interpretLastTradeMenu() {
        val menu = lastTradeMenu ?: return
        val trade = JsonObject()

        val (yourItems, yourCoins) = processSlots(menu, yourSlots)
        trade.add("yourItems", yourItems)
        trade.addProperty("yourCoins", yourCoins)

        val (theirItems, theirCoins) = processSlots(menu, theirSlots)
        trade.add("theirItems", theirItems)
        trade.addProperty("theirCoins", theirCoins)

        trade.addProperty("timestamp", System.currentTimeMillis())
        trade.addProperty("username", tradingWith)

        val date = Utils.getFormattedDate()
        if (!tradeHistory.has(date)) tradeHistory.add(date, JsonArray())
        tradeHistory[date].asJsonArray.add(trade)
        tradeData.forceSave()

        tradingWith = ""
    }

    private fun processSlots(menu: AbstractContainerMenu, slots: List<Int>): Pair<JsonArray, Long> {
        val items = JsonArray()
        var coins = 0L

        slots.forEach { slot ->
            menu.getSlot(slot).item?.let { stack ->
                if (stack.hoverName.string.removeFormatting().endsWith("coins")) {
                    coins += parseCoins(stack.hoverName.string.removeFormatting())
                } else {
                    if (!stack.isEmpty) items.add(createItemJson(stack))
                }
            }
        }

        return items to coins
    }

    private fun parseCoins(name: String): Long {
        return when {
            name.endsWith("k coins", true) -> name.dropLast(7).toDouble() * 1_000
            name.endsWith("M coins", true) -> name.dropLast(7).toDouble() * 1_000_000
            name.endsWith("B coins", true) -> name.dropLast(7).toDouble() * 1_000_000_000
            else -> name.replace(" coins", "").toDouble()
        }.toLong()
    }

    private fun createItemJson(stack: ItemStack): JsonObject {
        return JsonObject().apply {
            addProperty("count", stack.count)
            addProperty("lore", stack.getLore().joinToString("\n") { it.toLegacyString() })
            addProperty("name", stack.hoverName.toLegacyString())
            addProperty("id", BuiltInRegistries.ITEM.getKey(stack.item).toString())
            addProperty("skyblockId", stack.getData(DataTypes.SKYBLOCK_ID)?.skyblockId.toString())
        }
    }
}