package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils.addMessage
import meowing.zen.utils.ChatUtils.formatNumber
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object BetterBZ : Feature("betterbz") {
    private val patterns = mapOf(
        "instaBuy" to Regex("^\\[Bazaar] Bought ([\\d,]+(?:\\.\\d+)?)x (.+) for ([\\d,]+(?:\\.\\d+)?) coins!"),
        "buyOrderSetup" to Regex("^\\[Bazaar] Buy Order Setup! ([\\d,]+(?:\\.\\d+)?)x (.+) for ([\\d,]+(?:\\.\\d+)?) coins\\."),
        "buyOrderFilled" to Regex("^\\[Bazaar] Your Buy Order for ([\\d,]+(?:\\.\\d+)?)x (.+) was filled!"),
        "buyOrderCancelled" to Regex("^\\[Bazaar] Cancelled! Refunded ([\\d,]+(?:\\.\\d+)?) coins from cancelling Buy Order!"),
        "buyOrderClaimed" to Regex("^\\[Bazaar] Claimed ([\\d,]+(?:\\.\\d+)?)x (.+) worth ([\\d,]+(?:\\.\\d+)?) coins bought for ([\\d,]+(?:\\.\\d+)?) each!"),
        "instaSell" to Regex("^\\[Bazaar] Sold ([\\d,]+(?:\\.\\d+)?)x (.+) for ([\\d,]+(?:\\.\\d+)?) coins!"),
        "sellOfferSetup" to Regex("^\\[Bazaar] Sell Offer Setup! ([\\d,]+(?:\\.\\d+)?)x (.+) for ([\\d,]+(?:\\.\\d+)?) coins\\."),
        "sellOfferFilled" to Regex("^\\[Bazaar] Your Sell Offer for ([\\d,]+(?:\\.\\d+)?)x (.+) was filled!"),
        "sellOfferCancelled" to Regex("^\\[Bazaar] Cancelled! Refunded ([\\d,]+(?:\\.\\d+)?)x (.+) from cancelling Sell Offer!"),
        "sellOrderClaimed" to Regex("^\\[Bazaar] Claimed ([\\d,]+(?:\\.\\d+)?)x (.+) worth ([\\d,]+(?:\\.\\d+)?) coins sold for ([\\d,]+(?:\\.\\d+)?) each!")
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Clean Chat", "Better Messages", ConfigElement(
                "betterbz",
                "Better Bazaar",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            var cancelled = false

            patterns["instaBuy"]?.find(text)?.let {
                bzMessage("§c§lInsta-Bought! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["buyOrderSetup"]?.find(text)?.let {
                bzMessage("§c§lBuy Order Setup! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["buyOrderFilled"]?.find(text)?.let {
                bzMessage("§a§lBuy Order Filled! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r!")
                cancelled = true
            }

            patterns["buyOrderCancelled"]?.find(text)?.let {
                bzMessage("§c§lCancelled Order!§r Refunded §6${formatNumber(it.groups[1]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["buyOrderClaimed"]?.find(text)?.let {
                val total = formatNumber(it.groups[3]?.value ?: "")
                val per = formatNumber(it.groups[4]?.value ?: "")
                val each = if (total != per) "(§6${per}§r each!)" else ""
                bzMessage("Buy Order Claimed! §c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${total}§r coins! ${each}")
                cancelled = true
            }

            patterns["instaSell"]?.find(text)?.let {
                bzMessage("§c§lInsta-Sold! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["sellOfferSetup"]?.find(text)?.let {
                bzMessage("§c§lSell Offer Setup! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["sellOfferFilled"]?.find(text)?.let {
                bzMessage("§a§lSell Offer Filled! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r!")
                cancelled = true
            }

            patterns["sellOfferCancelled"]?.find(text)?.let {
                bzMessage("§c§lCancelled Order!§r Refunded §6${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r!")
                cancelled = true
            }

            patterns["sellOrderClaimed"]?.find(text)?.let {
                val total = formatNumber(it.groups[3]?.value ?: "")
                val per = formatNumber(it.groups[4]?.value ?: "")
                val each = if (total != per) "(§6${per}§r each!)" else ""
                bzMessage("Sell Order Claimed! §c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${total}§r coins! ${each}")
                cancelled = true
            }

            if (cancelled) event.cancel()
        }
    }

    private fun clean(item: String) = item.replace("ENCHANTED_", "").replace("_", " ").trim()
    private fun bzMessage(message: String) = addMessage("§6[BZ] §r$message")
}