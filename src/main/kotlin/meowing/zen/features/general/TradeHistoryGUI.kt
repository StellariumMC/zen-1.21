package meowing.zen.features.general

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.brigadier.context.CommandContext
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.UMatrixStack
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.api.TradeAPI
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.ui.components.ItemComponent
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Zen.Command
object TradeHistoryCommand : CommandUtils("tradelogs", aliases = listOf("zentl", "zentrades"))  {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        TickUtils.schedule(2) {
            mc.setScreen(TradeHistoryGui())
        }
        return 1
    }
}

class TradeHistoryGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(12, 16, 20, 255)
        val accent = Color(100, 245, 255, 255)
        val accent2 = Color(80, 200, 220, 255)
        val divider = Color(30, 35, 40, 255)
        val give = Color(255, 85, 85, 255)
        val receive = Color(85, 255, 85, 255)
        val warning = Color(255, 170, 0, 255)
    }

    private lateinit var scrollComponent: ScrollComponent
    private lateinit var tradesContainer: UIContainer
    private val formatter = DecimalFormat("#,###")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")
    private var searchQuery = ""
    private var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private var stackElements: MutableMap<UIComponent, ItemStack?> = mutableMapOf()
    private var drawContext: DrawContext? = null

    private fun UIComponent.addTooltip(tooltip: Set<String>, stack: ItemStack? = null) {
        tooltipElements[this] = tooltip
        stackElements[this] = stack
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)

        for (element in tooltipElements.keys) {
            if (element.isHovered()) {
                val tooltip = tooltipElements[element]?.map { Text.literal(it) } ?: mutableListOf()
                drawContext?.drawTooltip(mc.textRenderer, tooltip, mouseX, mouseY)
            }
        }
    }

    init {
        buildGui()
        updateTrades()
        EventBus.register<GuiEvent.AfterRender> { drawContext = it.context }
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 90.percent()
        }.setColor(theme.accent2) childOf window

        val main = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.bg) childOf border

        createHeader(main)
        createContent(main)
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 40.pixels()
        } childOf parent

        UIText("§lTrade History").constrain {
            x = 12.pixels()
            y = 12.pixels()
            textScale = 2.0.pixels()
        }.setColor(theme.accent) childOf header

        val searchContainer = createBlock(3f).constrain {
            x = 12.pixels(true)
            y = CenterConstraint()
            width = 200.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf header

        val searchInput = (UITextInput("Search trades...").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 16.pixels()
            height = 14.pixels()
        }.setColor(Color.LIGHT_GRAY) childOf searchContainer) as UITextInput

        searchInput.onKeyType { _, _ ->
            searchQuery = searchInput.getText()
            updateTrades()
        }

        searchContainer.onMouseClick {
            searchInput.grabWindowFocus()
        }

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf header
    }

    private fun createContent(parent: UIComponent) {
        val contentPanel = UIContainer().constrain {
            x = 8.pixels()
            y = 48.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 56.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        tradesContainer = UIContainer().constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf scrollComponent

        window.onMouseScroll {
            it.stopImmediatePropagation()
            scrollComponent.mouseScroll(it.delta)
        }
    }

    private fun updateTrades() {
        tradesContainer.clearChildren()
        tooltipElements.clear()
        stackElements.clear()

        val tradeHistory = TradeAPI.getTradeHistory()
        if (tradeHistory.entrySet().isEmpty()) {
            createEmptyState()
            return
        }

        val sortedDates = tradeHistory.entrySet().map { it.key }.sortedByDescending { it }

        sortedDates.forEach { date ->
            val trades = tradeHistory.getAsJsonArray(date)
            if (trades.size() > 0 && dateShouldShow(date, trades)) {
                createDateSection(date, trades)
            }
        }
    }

    private fun createEmptyState() {
        val emptyContainer = UIContainer().constrain {
            x = 0.percent()
            y = 50.percent()
            width = 100.percent()
            height = 60.pixels()
        } childOf tradesContainer

        UIText("No trades recorded yet").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.5.pixels()
        }.setColor(theme.accent2) childOf emptyContainer
    }

    private fun createDateSection(date: String, trades: JsonArray) {
        val dateContainer = UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(12f)
            width = 100.percent()
            height = 40.pixels()
        } childOf tradesContainer

        val dateHeader = createBlock(3f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 35.pixels()
        }.setColor(theme.element) childOf dateContainer

        UIText(date).constrain {
            x = 12.pixels()
            y = CenterConstraint()
            textScale = 1.3.pixels()
        }.setColor(theme.accent) childOf dateHeader

        UIText("${trades.size()} trades").constrain {
            x = 12.pixels(true)
            y = CenterConstraint()
            textScale = 1.0.pixels()
        }.setColor(theme.accent2) childOf dateHeader

        val dateTooltip = createDateSummary(date, trades)
        dateHeader.addTooltip(dateTooltip)

        val horizontalScroll = ScrollComponent(horizontalScrollEnabled = true, verticalScrollEnabled = false).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(4f)
            width = 100.percent()
            height = 160.pixels()
        } childOf tradesContainer

        horizontalScroll.onMouseScroll {
            if (!InputUtil.isKeyPressed(mc.window.handle, GLFW.GLFW_KEY_LEFT_SHIFT)) {
                it.stopImmediatePropagation()
                scrollComponent.scrollTo(verticalOffset = (scrollComponent.verticalOffset + it.delta * 20).toFloat())
            }
        }

        val scrollbar = createBlock(3f).constrain {
            width = 100.percent()
            height = 4.pixels()
            y = 100.percent() - 4.pixels()
            color = theme.divider.constraint
        } childOf horizontalScroll.parent

        horizontalScroll.setHorizontalScrollBarComponent(scrollbar, true)

        trades.reversed().forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            if (matchesSearch(trade, date)) {
                createTradeCard(horizontalScroll, trade)
            }
        }
    }

    private fun createDateSummary(date: String, trades: JsonArray): Set<String> {
        var coinChange = 0L
        val itemChanges = mutableMapOf<String, Int>()

        trades.forEach { tradeElement ->
            val trade = tradeElement.asJsonObject
            coinChange += trade.get("theirCoins").asLong - trade.get("yourCoins").asLong

            trade.getAsJsonArray("yourItems").forEach { item ->
                val itemObj = item.asJsonObject
                val stack = createItemStack(itemObj)
                val key = stack.name.string.removeFormatting()
                itemChanges[key] = itemChanges.getOrDefault(key, 0) - stack.count
            }

            trade.getAsJsonArray("theirItems").forEach { item ->
                val itemObj = item.asJsonObject
                val stack = createItemStack(itemObj)
                val key = stack.name.string.removeFormatting()
                itemChanges[key] = itemChanges.getOrDefault(key, 0) + stack.count
            }
        }

        val tooltip = mutableSetOf<String>()
        tooltip.add("§e§l$date Summary")
        tooltip.add("§7Coin Change: ${if (coinChange >= 0) "§a+" else "§c"}${formatter.format(coinChange)}")

        if (itemChanges.isNotEmpty()) {
            tooltip.add("§7Item Changes:")
            itemChanges.forEach { (item, change) ->
                if (change != 0) {
                    tooltip.add(" ${if (change > 0) "§a+" else "§c"}$change §7$item")
                }
            }
        }

        return tooltip
    }

    private fun createTradeCard(parent: UIComponent, trade: JsonObject) {
        val card = createBlock(3f).constrain {
            x = SiblingConstraint(8f)
            y = 0.percent()
            width = 220.pixels()
            height = 150.pixels()
        }.setColor(theme.element) childOf parent

        val timestamp = trade.get("timestamp").asLong
        val username = trade.get("username").asString
        val yourCoins = trade.get("yourCoins").asLong
        val theirCoins = trade.get("theirCoins").asLong
        val yourItems = trade.getAsJsonArray("yourItems")
        val theirItems = trade.getAsJsonArray("theirItems")

        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 30.pixels()
        } childOf card

        UIText("§b$username").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 1.1.pixels()
        }.setColor(theme.accent2) childOf header

        UIText(timeFormatter.format(Date(timestamp))).constrain {
            x = 8.pixels(true)
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(theme.divider) childOf header

        createBlock(0f).constrain {
            x = 4.pixels()
            y = 100.percent() - 1.pixels()
            width = 100.percent() - 8.pixels()
            height = 1.pixels()
        }.setColor(theme.divider) childOf header

        val contentArea = UIContainer().constrain {
            x = 0.percent()
            y = 32.pixels()
            width = 100.percent()
            height = 118.pixels()
        } childOf card

        val leftSide = UIContainer().constrain {
            x = 4.pixels()
            y = 0.percent()
            width = 50.percent() - 6.pixels()
            height = 100.percent()
        } childOf contentArea

        val rightSide = UIContainer().constrain {
            x = 50.percent() + 2.pixels()
            y = 0.percent()
            width = 50.percent() - 6.pixels()
            height = 100.percent()
        } childOf contentArea

        createTradeSide(leftSide, "You Gave", yourItems, yourCoins, theme.give)
        createTradeSide(rightSide, "You Received", theirItems, theirCoins, theme.receive)

        UIBlock().constrain {
            x = 50.percent()
            y = 0.percent() - 2.pixels()
            width = 1.pixels()
            height = 90.percent()
        }.setColor(theme.divider) childOf contentArea

        val profit = theirCoins - yourCoins
        val profitText = if (profit >= 0) "§a+${formatter.format(profit)}" else "§c${formatter.format(profit)}"

        UIText("Profit: $profitText").constrain {
            x = CenterConstraint()
            y = 100.percent() - 10.pixels()
            textScale = 0.9.pixels()
        }.setColor(if (profit >= 0) theme.receive else theme.give) childOf card
    }

    private fun createTradeSide(parent: UIComponent, title: String, items: JsonArray, coins: Long, color: Color) {
        UIText(title).constrain {
            x = CenterConstraint()
            y = 4.pixels()
            textScale = 0.9.pixels()
        }.setColor(color) childOf parent

        val itemsList = UIContainer().constrain {
            x = 0.percent()
            y = 20.pixels()
            width = 100.percent()
            height = 64.pixels()
        } childOf parent

        val filteredItems = items.filter { itemElement ->
            val itemObj = itemElement.asJsonObject
            val stack = createItemStack(itemObj)
            !stack.name.string.removeFormatting().equals("Air", ignoreCase = true)
        }

        filteredItems.take(3).forEachIndexed { index, itemElement ->
            val itemObj = itemElement.asJsonObject
            val stack = createItemStack(itemObj)

            val itemName = UIText("${stack.count}x ${stack.name.string}").constrain {
                x = 2.pixels()
                y = (index * 16).pixels()
                textScale = 0.8.pixels()
            }.setColor(Color.WHITE) childOf itemsList

            val tooltip = mutableSetOf<String>()
            tooltip.add(stack.name.string)
            tooltip.add("§7Count: ${stack.count}")

            itemName.addTooltip(tooltip)
        }

        if (filteredItems.size > 3) {
            UIText("§7+${items.size() - 3} more").constrain {
                x = 2.pixels()
                y = 70.pixels()
                textScale = 0.7.pixels()
            }.setColor(theme.accent2) childOf parent
        }

        if (coins > 0) {
            UIText("§6${formatter.format(coins)} coins").constrain {
                x = CenterConstraint()
                y = 100.percent() - 25.pixels()
                textScale = 0.8.pixels()
            }.setColor(theme.warning) childOf parent
        }
    }

    private fun createItemStack(itemObj: JsonObject): ItemStack {
        val item = Registries.ITEM.get(Identifier.of(itemObj.get("id").asString))
        val stack = ItemStack(item, itemObj.get("count").asInt)
        stack.damage = itemObj.get("damage").asInt
        return stack
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
                val stack = createItemStack(itemElement.asJsonObject)
                if (stack.name.string.removeFormatting().contains(searchQuery, ignoreCase = true)) return true
            }
        }

        return false
    }
}