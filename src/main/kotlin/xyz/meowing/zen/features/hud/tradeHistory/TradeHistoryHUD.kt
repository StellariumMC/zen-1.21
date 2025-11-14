package xyz.meowing.zen.features.hud.tradeHistory

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import xyz.meowing.knit.api.input.KnitKeyboard
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.knit.api.screen.KnitScreen
import xyz.meowing.zen.api.item.ItemAPI
import xyz.meowing.zen.api.skyblock.TradeAPI
import xyz.meowing.zen.ui.Theme
import xyz.meowing.zen.utils.Render2D.pushPop
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

//#if MC >= 1.21.9
//$$ import xyz.meowing.zen.utils.Render2D.renderOutline
//#endif

class TradeHistoryHUD : KnitScreen("Trade History") {
    private val formatter = DecimalFormat("#,###")
    private val timeFormatter = SimpleDateFormat("HH:mm")
    private var horizontalScrollOffsets = mutableMapOf<String, Float>()
    private var maxHorizontalScrolls = mutableMapOf<String, Float>()
    private var currentScrollingDate: String? = null
    private var searchQuery = ""
    private var scrollOffset = 0f
    private var maxScroll = 0f
    private var searchFocused = false
    private var searchCursorPos = 0
    private val padding = 8
    private val headerHeight = 30
    private val searchBoxWidth = 200
    private val searchBoxHeight = 20
    private val cardWidth = 220
    private val cardHeight = 150
    private val cardSpacing = 4
    private val itemSize = 12

    private data class HoverElement(val x: Int, val y: Int, val width: Int, val height: Int, val tooltip: List<Component>)
    private data class ItemRender(val stack: ItemStack, val x: Int, val y: Int, val scale: Float)

    private val hoverElements = mutableListOf<HoverElement>()
    private val itemsToRender = mutableListOf<ItemRender>()

    override fun onRender(context: GuiGraphics?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        if (context == null) return
        hoverElements.clear()
        itemsToRender.clear()

        val screenWidth = width
        val screenHeight = height
        val mainWidth = (screenWidth * 0.9f).toInt()
        val mainHeight = (screenHeight * 0.9f).toInt()
        val mainX = (screenWidth - mainWidth) / 2
        val mainY = (screenHeight - mainHeight) / 2

        context.fill(mainX, mainY, mainX + mainWidth, mainY + mainHeight, Theme.BgDark.color)
        context.renderOutline(mainX, mainY, mainWidth, mainHeight, Theme.Primary.color)

        renderHeader(context, mainX, mainY, mainWidth)
        renderContent(context, mainX, mainY + headerHeight, mainWidth, mainHeight - headerHeight, mouseX, mouseY)

        context.enableScissor(mainX + padding, mainY + headerHeight, mainX + mainWidth - padding, mainY + mainHeight)

        for (item in itemsToRender) {
            context.pushPop {
                //#if MC >= 1.21.7
                //$$ context.pose().translate(item.x.toFloat(), item.y.toFloat())
                //$$ context.pose().scale(item.scale, item.scale)
                //#else
                context.pose().translate(item.x.toFloat(), item.y.toFloat(), 0f)
                context.pose().scale(item.scale, item.scale, 1f)
                //#endif
                context.renderItem(item.stack, 0, 0)
                context.renderItemDecorations(font, item.stack, 0, 0)
            }
        }

        context.disableScissor()

        for (element in hoverElements) {
            if (mouseX >= element.x && mouseX <= element.x + element.width &&
                mouseY >= element.y && mouseY <= element.y + element.height) {
                context.renderComponentTooltip(font, element.tooltip, mouseX, mouseY)
                break
            }
        }
    }

    private fun renderHeader(context: GuiGraphics, x: Int, y: Int, width: Int) {
        context.drawString(font, "Trade History", x + padding, y + (headerHeight - font.lineHeight) / 2, Theme.Text.color, false)

        val searchX = x + width - searchBoxWidth - (padding / 2)
        val searchY = y + (headerHeight - searchBoxHeight) / 2

        val mouseX = KnitMouse.Scaled.x
        val mouseY = KnitMouse.Scaled.y
        val searchHovered = mouseX >= searchX && mouseX <= searchX + searchBoxWidth && mouseY >= searchY && mouseY <= searchY + searchBoxHeight

        val searchBg = if (searchHovered) Theme.BgLight.color else Theme.Bg.color
        context.fill(searchX, searchY, searchX + searchBoxWidth, searchY + searchBoxHeight, searchBg)

        val displayText = if (searchQuery.isEmpty() && !searchFocused) "Search trades..." else searchQuery
        val textColor = if (searchQuery.isEmpty() && !searchFocused) 0x80aac7ff.toInt() else Theme.Text.color
        context.drawString(font, displayText, searchX + 6, searchY + (searchBoxHeight - font.lineHeight + 2) / 2, textColor, false)

        if (searchFocused && System.currentTimeMillis() % 1000 < 500) {
            val cursorX = searchX + 6 + font.width(searchQuery.substring(0, min(searchCursorPos, searchQuery.length)))
            context.fill(cursorX, searchY + 4, cursorX + 1, searchY + searchBoxHeight - 4, Theme.Text.color)
        }

        context.fill(x, y + headerHeight - 1, x + width, y + headerHeight, Theme.Primary.withAlpha(0.5f))
    }

    private fun renderContent(context: GuiGraphics, x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int) {
        val tradeHistory = TradeAPI.tradeHistory
        if (tradeHistory.entrySet().isEmpty()) {
            context.drawCenteredString(font, "No trades recorded yet", x + width / 2, y + height / 2, Theme.Text.color)
            return
        }

        context.enableScissor(x, y, x + width, y + height)

        val sortedDates = tradeHistory.entrySet().map { it.key }.sortedByDescending { it }
        var currentY = y + padding - scrollOffset.toInt()

        for (date in sortedDates) {
            val trades = tradeHistory.getAsJsonArray(date)
            if (trades.size() > 0 && dateShouldShow(date, trades)) {
                currentY = renderDateSection(context, x, currentY, width, date, trades, mouseX, mouseY)
                currentY += padding
            }
        }

        maxScroll = max(0f, currentY + scrollOffset - y - height + padding)

        if (maxScroll > 0) {
            val scrollbarHeight = max(20, (height * height / (height + maxScroll)).toInt())
            val scrollbarY = y + (scrollOffset / maxScroll * (height - scrollbarHeight)).toInt()
            context.fill(x + width - 6, scrollbarY, x + width - 3, scrollbarY + scrollbarHeight, 0xFF7c7c7d.toInt())
        }

        context.disableScissor()
    }

    private fun renderDateSection(context: GuiGraphics, x: Int, y: Int, width: Int, date: String, trades: JsonArray, mouseX: Int, mouseY: Int): Int {
        val headerHeight = 25

        context.fill(x + padding, y, x + width - padding, y + headerHeight, Theme.Bg.color)
        context.drawString(font, date, x + padding * 2, y + (headerHeight - font.lineHeight) / 2, Theme.Text.color, false)
        context.drawString(font, "${trades.size()} trades", x + width - padding * 2 - font.width("${trades.size()} trades"), y + (headerHeight - font.lineHeight) / 2, Theme.TextMuted.color, false)

        hoverElements.add(HoverElement(x + padding, y, width - padding * 2, headerHeight, createDateSummary(date, trades)))

        val scrollY = y + headerHeight
        val scrollHeight = cardHeight + 10
        val scrollWidth = width - padding * 2

        val filteredTrades = trades.filter { matchesSearch(it.asJsonObject, date) }
        val totalContentWidth = filteredTrades.size * (cardWidth + cardSpacing) + cardSpacing
        val maxHScroll = max(0f, totalContentWidth - scrollWidth.toFloat())
        maxHorizontalScrolls[date] = maxHScroll

        val hScrollOffset = horizontalScrollOffsets.getOrDefault(date, 0f)

        if (mouseX >= x + padding && mouseX <= x + width - padding &&
            mouseY >= scrollY && mouseY <= scrollY + scrollHeight) {
            currentScrollingDate = date
        }

        context.enableScissor(x + padding, scrollY, x + width - padding, scrollY + scrollHeight)

        var cardX = x + padding + cardSpacing - hScrollOffset.toInt()

        for (trade in filteredTrades.reversed()) {
            renderTradeCard(context, cardX, scrollY + 5, trade.asJsonObject)
            cardX += cardWidth + cardSpacing
        }

        context.disableScissor()

        if (maxHScroll > 0) {
            val scrollbarWidth = max(40, (scrollWidth * scrollWidth / totalContentWidth))
            val scrollbarX = x + padding + (hScrollOffset / maxHScroll * (scrollWidth - scrollbarWidth)).toInt()
            context.fill(scrollbarX, scrollY + scrollHeight, scrollbarX + scrollbarWidth, scrollY + scrollHeight - 1, 0xFF7c7c7d.toInt())
        }

        return scrollY + scrollHeight + 4
    }

    private fun renderTradeCard(context: GuiGraphics, x: Int, y: Int, trade: JsonObject) {
        context.fill(x, y, x + cardWidth, y + cardHeight, Theme.Bg.color)
        context.renderOutline(x, y, cardWidth, cardHeight, Theme.Primary.color)

        val timestamp = trade.get("timestamp").asLong
        val username = trade.get("username").asString
        val yourCoins = trade.get("yourCoins").asLong
        val theirCoins = trade.get("theirCoins").asLong
        val yourItems = trade.getAsJsonArray("yourItems")
        val theirItems = trade.getAsJsonArray("theirItems")

        context.drawString(font, username, x + 8, y + 10, Theme.Warning.color, false)
        context.drawString(font, timeFormatter.format(Date(timestamp)), x + cardWidth - 8 - font.width(timeFormatter.format(Date(timestamp))), y + 10, Theme.TextMuted.color, false)
        context.fill(x, y + 29, x + cardWidth, y + 30, Theme.Primary.color)

        val leftX = x + 8
        val rightX = x + cardWidth / 2 + 8
        val contentY = y + 35

        val yourCustomWorth = renderTradeSide(context, leftX, contentY, "You Gave", yourItems, yourCoins, Theme.Danger.color, trade, "yourCustomValue")
        val theirCustomWorth = renderTradeSide(context, rightX, contentY, "You Received", theirItems, theirCoins, Theme.Success.color, trade, "theirCustomValue")

        context.fill(x + cardWidth / 2, y + 40, x + cardWidth / 2 + 1, y + cardHeight - 30, Theme.Primary.color)

        val profit = theirCustomWorth - yourCustomWorth
        val profitText = if (profit >= 0) "Profit: +${abbreviateNumber(profit)}" else "Loss: ${abbreviateNumber(profit)}"
        val profitColor = if (profit >= 0) Theme.Success.color else Theme.Danger.color
        context.drawString(font, profitText, x + (cardWidth - font.width(profitText)) / 2, y + cardHeight - 20, profitColor, false)
    }

    private fun renderTradeSide(context: GuiGraphics, x: Int, y: Int, title: String, items: JsonArray, coins: Long, color: Int, trade: JsonObject, customValueKey: String): Long {
        val titleWidth = font.width(title)
        val sectionWidth = cardWidth / 2 - 16
        context.drawString(font, title, x + (sectionWidth - titleWidth) / 2, y, color, false)

        val itemsStartY = y + 15
        var itemWorth = 0L
        val maxCols = 4
        val gridWidth = maxCols * (itemSize + 4) - 4
        val startX = x + (sectionWidth - gridWidth) / 2

        items.forEachIndexed { index, itemElement ->
            val jsonObject = itemElement.asJsonObject
            val stack = createItemStack(jsonObject)
            val loreLines = jsonObject.get("lore").asString.split('\n').map { Component.literal(it) }
            val name = jsonObject.get("name").asString
            val skyblockId = if (jsonObject.has("skyblockId")) jsonObject.get("skyblockId").asString else "null"

            val row = index / maxCols
            val col = index % maxCols
            val itemX = startX + col * (itemSize + 4)
            val itemY = itemsStartY + row * (itemSize + 4)

            itemsToRender.add(ItemRender(stack, itemX, itemY, 0.75f))

            val tooltip = mutableListOf<Component>()
            tooltip.add(Component.literal(name))
            tooltip.addAll(loreLines)

            hoverElements.add(HoverElement(itemX, itemY, itemSize, itemSize, tooltip))
            itemWorth += getItemValue(skyblockId) * stack.count
        }

        val totalWorth = itemWorth + coins
        var currentWorth = totalWorth

        if (trade.has(customValueKey)) {
            currentWorth = trade.get(customValueKey).asLong
        }

        if (coins > 0) {
            val coinText = "${abbreviateNumber(coins)} coins"
            val coinWidth = font.width(coinText)
            context.drawString(font, coinText, x + (sectionWidth - coinWidth) / 2, y + 70, Theme.Warning.color, false)
        }

        return currentWorth
    }

    private fun createDateSummary(date: String, trades: JsonArray): List<Component> {
        var coinChange = 0L
        val itemChanges = mutableMapOf<String, Int>()

        trades.forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            coinChange += trade.get("theirCoins").asLong - trade.get("yourCoins").asLong

            trade.getAsJsonArray("yourItems").forEach { item ->
                val itemObj = item.asJsonObject
                val stack = createItemStack(itemObj)
                val key = stack.hoverName.string.removeFormatting()
                itemChanges[key] = itemChanges.getOrDefault(key, 0) - stack.count
            }

            trade.getAsJsonArray("theirItems").forEach { item ->
                val itemObj = item.asJsonObject
                val stack = createItemStack(itemObj)
                val key = stack.hoverName.string.removeFormatting()
                itemChanges[key] = itemChanges.getOrDefault(key, 0) + stack.count
            }
        }

        val tooltip = mutableListOf<Component>()
        tooltip.add(Component.literal("§e§l$date Summary"))
        tooltip.add(Component.literal("§7Coin Change: ${if (coinChange >= 0) "§a+" else "§c"}${formatter.format(coinChange)}"))

        if (itemChanges.isNotEmpty()) {
            tooltip.add(Component.literal("§7Item Changes:"))
            itemChanges.forEach { (item, change) ->
                if (change != 0) {
                    tooltip.add(Component.literal(" ${if (change > 0) "§a+" else "§c"}$change §7$item"))
                }
            }
        }

        return tooltip
    }

    private fun getItemValue(skyblockId: String): Long {
        if (skyblockId == "null") return 0L
        val itemInfo = ItemAPI.getItemInfo(skyblockId) ?: return 0L

        return when {
            itemInfo.has("lowestBin") && itemInfo.get("lowestBin").asLong > 0 -> itemInfo.get("lowestBin").asLong
            itemInfo.has("bazaarSell") && itemInfo.get("bazaarSell").asDouble > 0 -> itemInfo.get("bazaarSell").asDouble.toLong()
            itemInfo.has("bazaarBuy") && itemInfo.get("bazaarBuy").asDouble > 0 -> itemInfo.get("bazaarBuy").asDouble.toLong()
            itemInfo.has("avgLowestBin") && itemInfo.get("avgLowestBin").asLong > 0 -> itemInfo.get("avgLowestBin").asLong
            itemInfo.has("npcSell") && itemInfo.get("npcSell").asLong > 1 -> itemInfo.get("npcSell").asLong
            else -> 0L
        }
    }

    private fun createItemStack(itemObj: JsonObject): ItemStack {
        val item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(itemObj.get("id").asString))
        return ItemStack(item, itemObj.get("count").asInt)
    }

    private fun dateShouldShow(date: String, trades: JsonArray): Boolean {
        if (searchQuery.isEmpty()) return true
        if (date.contains(searchQuery, ignoreCase = true)) return true

        trades.forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            if (matchesSearch(trade, date)) return true
        }
        return false
    }

    private fun matchesSearch(trade: JsonObject, date: String): Boolean {
        if (searchQuery.isEmpty()) return true
        if (date.contains(searchQuery, ignoreCase = true)) return true
        if (trade.get("username").asString.contains(searchQuery, ignoreCase = true)) return true

        listOf("yourItems", "theirItems").forEach { itemsKey ->
            trade.getAsJsonArray(itemsKey).forEach { itemElement ->
                val name = itemElement.asJsonObject.get("name").asString
                if (name.removeFormatting().contains(searchQuery, ignoreCase = true)) return true
            }
        }

        return false
    }

    private fun abbreviateNumber(num: Long): String {
        return when {
            num >= 1_000_000_000_000 -> "${(num / 1_000_000_000_000.0).format()}T"
            num >= 1_000_000_000 -> "${(num / 1_000_000_000.0).format()}B"
            num >= 1_000_000 -> "${(num / 1_000_000.0).format()}M"
            num >= 1_000 -> "${(num / 1_000.0).format()}K"
            else -> num.toString()
        }
    }

    private fun Double.format(): String {
        return if (this == this.toInt().toDouble()) this.toInt().toString() else String.format("%.1f", this)
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        val searchX = width - (width * 0.05f).toInt() - searchBoxWidth - padding
        val searchY = (height * 0.05f).toInt() + (headerHeight - searchBoxHeight) / 2

        searchFocused = mouseX >= searchX && mouseX <= searchX + searchBoxWidth &&
                mouseY >= searchY && mouseY <= searchY + searchBoxHeight

        if (searchFocused) {
            searchCursorPos = searchQuery.length
        }
    }

    override fun onKeyType(typedChar: Char, keyCode: Int, scanCode: Int) {
        if (!searchFocused) return

        when {
            keyCode == 259 && searchQuery.isNotEmpty() -> {
                searchQuery = searchQuery.dropLast(1)
                searchCursorPos = searchQuery.length
            }
            keyCode == 257 || keyCode == 335 -> {
                searchFocused = false
            }
            typedChar.isLetterOrDigit() || typedChar == ' ' -> {
                searchQuery += typedChar
                searchCursorPos = searchQuery.length
            }
        }
    }

    override fun onMouseScroll(horizontal: Double, vertical: Double) {
        if (currentScrollingDate != null && KnitKeyboard.isShiftKeyPressed  ) {
            val date = currentScrollingDate!!
            val maxHScroll = maxHorizontalScrolls.getOrDefault(date, 0f)
            if (maxHScroll > 0) {
                val currentOffset = horizontalScrollOffsets.getOrDefault(date, 0f)
                horizontalScrollOffsets[date] = (currentOffset - horizontal.toFloat() * 20 - vertical.toFloat() * 20).coerceIn(0f, maxHScroll)
            }
            currentScrollingDate = null
        } else {
            scrollOffset = (scrollOffset - vertical.toFloat() * 20).coerceIn(0f, maxScroll)
        }
    }
}