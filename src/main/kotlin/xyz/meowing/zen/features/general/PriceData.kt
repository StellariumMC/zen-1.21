package xyz.meowing.zen.features.general

import xyz.meowing.zen.api.item.ItemAPI
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.Timer
import xyz.meowing.zen.utils.ItemUtils.displayName
import xyz.meowing.zen.utils.Utils.abbreviateNumber
import xyz.meowing.zen.utils.Utils.formatNumber
import net.minecraft.text.Text
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ItemTooltipEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object PriceData : Feature(
    "priceData",
    true
) {
    private val displayOptions = listOf(
        "Active Listings",
        "Daily Sales",
        "BIN Price",
        "Auction Price",
        "Bazaar",
        "Raw Craft Cost"
    )

    data class CacheEntry(
        val lines: List<Text>,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val tooltipCache = mutableMapOf<String, CacheEntry>()

    private val displaySet by ConfigDelegate<Set<Int>>("priceData.display")
    private val abbreviateNumbers by ConfigDelegate<Boolean>("priceData.abbreviateNumbers")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Price data",
                "Shows price information for items",
                "General",
                ConfigElement(
                    "priceData",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Info to show",
                ConfigElement(
                    "priceData.display",
                    ElementType.MultiCheckbox(displayOptions, setOf(0, 1, 2, 3, 4))
                )
            )
            .addFeatureOption(
                "Abbreviate numbers",
                ConfigElement(
                    "priceData.abbreviateNumbers",
                    ElementType.Switch(false)
                )
            )
    }

    private fun Number.formatPrice(): String = if (abbreviateNumbers) abbreviateNumber() else formatNumber()

    override fun initialize() {
        setupLoops {
            loop<Timer>(60000) {
                val currentTime = System.currentTimeMillis()
                tooltipCache.entries.removeAll { (_, entry) ->
                    currentTime - entry.timestamp > 60000
                }
            }
        }

        register<ItemTooltipEvent>(priority = 10) { event ->
            val stack = event.stack
            val itemUuid = stack.displayName()

            val cacheKey = "${itemUuid}_${stack.count}_${displaySet.hashCode()}_${abbreviateNumbers}"

            tooltipCache[cacheKey]?.let { cacheEntry ->
                event.lines.addAll(cacheEntry.lines)
                return@register
            }

            val pricingData = ItemAPI.getItemInfo(stack) ?: return@register
            val priceLines = mutableListOf<Text>()

            if (0 in displaySet) {
                pricingData.takeIf { it.has("activeBin") || it.has("activeAuc") }?.let {
                    val activeBinNum = if (it.has("activeBin")) it.get("activeBin").asInt else -1
                    val activeAucNum = if (it.has("activeAuc")) it.get("activeAuc").asInt else -1
                    val activeBin = if (activeBinNum != -1) activeBinNum.formatPrice() else "§7N/A"
                    val activeAuc = if (activeAucNum != -1) activeAucNum.formatPrice() else "§7N/A"
                    priceLines.add(Text.literal("§3Active Listings: §e${activeBin} §8[BIN] §7• §e${activeAuc} §8[Auction]"))
                }
            }

            if (1 in displaySet) {
                pricingData.takeIf { it.has("binSold") || it.has("aucSold") }?.let {
                    val soldBinNum = if (it.has("binSold")) it.get("binSold").asInt else -1
                    val soldAucNum = if (it.has("aucSold")) it.get("aucSold").asInt else -1
                    val soldBin = if (soldBinNum != -1) soldBinNum.formatPrice() else "§7N/A"
                    val soldAuc = if (soldAucNum != -1) soldAucNum.formatPrice() else "§7N/A"
                    priceLines.add(Text.literal("§3Daily Sales: §e${soldBin} §8[BIN] §7• §e${soldAuc} §8[Auction]"))
                }
            }

            if (2 in displaySet) {
                pricingData.takeIf { it.has("avgLowestBin") && it.has("lowestBin") }?.let {
                    val avgLowestBin = it.get("avgLowestBin").asLong.formatPrice()
                    val lowestBin = it.get("lowestBin").asLong.formatPrice()
                    priceLines.add(Text.literal("§3BIN Price: §a${avgLowestBin} §8[Avg] §7• §a${lowestBin} §8[Lowest]"))
                }
            }

            if (3 in displaySet) {
                pricingData.takeIf { it.has("avgAucPrice") && it.has("aucPrice") }?.let {
                    val avgAucPrice = it.get("avgAucPrice").asLong.formatPrice()
                    val aucPrice = it.get("aucPrice").asLong.formatPrice()
                    priceLines.add(Text.literal("§3Auction Price: §a${avgAucPrice} §8[Avg] §7• §a${aucPrice} §8[Next]"))
                }
            }

            if (4 in displaySet) {
                pricingData.takeIf { it.has("bazaarBuy") || it.has("bazaarSell") }?.let {
                    val multiplier = stack.count
                    val bazaarBuy = it.takeIf { it.has("bazaarBuy") }
                        ?.get("bazaarBuy")
                        ?.asLong
                        ?.times(multiplier)
                        ?.formatPrice() ?: "§7N/A"
                    val bazaarSell = it.takeIf { it.has("bazaarSell") }
                        ?.get("bazaarSell")
                        ?.asLong
                        ?.times(multiplier)
                        ?.formatPrice() ?: "§7N/A"
                    priceLines.add(Text.literal("§3Bazaar: §e${bazaarBuy} §8[Buy] §7• §a${bazaarSell} §8[Sell]"))
                }
            }

            if (5 in displaySet) {
                val rawCraftCost = stack.getItemValue().price.formatPrice()
                priceLines.add(Text.literal("§3Raw Craft Cost: §a$rawCraftCost"))
            }

            if (priceLines.isNotEmpty()) {
                event.lines.addAll(priceLines)
                tooltipCache[cacheKey] = CacheEntry(priceLines)
            }
        }
    }
}