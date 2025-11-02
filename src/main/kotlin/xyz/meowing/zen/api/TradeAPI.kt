package xyz.meowing.zen.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.ktmodules.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.chestName
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.GuiEvent

@Module
object TradeAPI {
    data class TradeLogs(val tradeHistory: JsonObject = JsonObject())

    private val save = DataUtils("TradeAPI", TradeLogs())
    private var inTradeMenu = false
    private var lastTradeMenu: ScreenHandler? = null
    private var tradingWith = ""
    private var tradingWithSub = ""

    private val yourSlots = listOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30)
    private val theirSlots = listOf(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35)

    init {
        TickUtils.loop(20) {
            if (world == null) return@loop
            if (client.currentScreen == null) inTradeMenu = false

            if (tradingWithSub.isNotEmpty()) {
                world!!.players.find { it.name.string.contains(tradingWithSub) }?.let {
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
            if (event.slot.id == 0) {
                inTradeMenu = false
                val tradeSlot = event.screen.screenHandler.slots?.get(4) ?: return@register
                if (tradeSlot.stack?.name?.string?.removeFormatting() != "⇦ Your stuff") return@register

                inTradeMenu = true
                lastTradeMenu = event.screen.screenHandler
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
        save.updateAndSave {
            if (!tradeHistory.has(date)) tradeHistory.add(date, JsonArray())
            tradeHistory[date].asJsonArray.add(trade)
        }

        tradingWith = ""
    }

    private fun processSlots(menu: ScreenHandler, slots: List<Int>): Pair<JsonArray, Long> {
        val items = JsonArray()
        var coins = 0L

        slots.forEach { slot ->
            menu.getSlot(slot).stack?.let { stack ->
                if (stack.name.string.removeFormatting().endsWith("coins")) {
                    coins += parseCoins(stack.name.string.removeFormatting())
                } else {
                    if (!stack.isEmpty)
                        items.add(createItemJson(stack))
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
            addProperty("lore", stack.lore.joinToString("\n"))
            addProperty("name", stack.name.string)
            addProperty("id", Registries.ITEM.getId(stack.item).toString())
        }
    }

    fun getTradeHistory(): JsonObject = save().tradeHistory
}