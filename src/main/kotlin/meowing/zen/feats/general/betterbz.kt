package meowing.zen.feats.general

import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils.addMessage
import meowing.zen.utils.ChatUtils.formatNumber
import meowing.zen.utils.Utils.removeFormatting

object betterbz : Feature("betterbz") {
    private val patterns = mapOf(
        "instaBuy" to Regex("\\[Bazaar] Bought ([\\d,]+)x (.+) for ([\\d,]+) coins!"),
        "buyOrderSetup" to Regex("\\[Bazaar] Buy Order Setup! ([\\d,]+)x (.+) for ([\\d,]+) coins\\."),
        "buyOrderFilled" to Regex("\\[Bazaar] Your Buy Order for ([\\d,]+)x (.+) was filled!"),
        "buyOrderCancelled" to Regex("\\[Bazaar] Cancelled! Refunded ([\\d,]+) coins from cancelling Buy Order!"),
        "buyOrderClaimed" to Regex("\\[Bazaar] Claimed ([\\d,]+)x (.+) worth ([\\d,]+) coins bought for ([\\d,]+) each!"),
        "instaSell" to Regex("\\[Bazaar] Sold ([\\d,]+)x (.+) for ([\\d,]+) coins!"),
        "sellOfferSetup" to Regex("\\[Bazaar] Sell Offer Setup! ([\\d,]+)x (.+) for ([\\d,]+) coins\\."),
        "sellOfferFilled" to Regex("\\[Bazaar] Your Sell Offer for ([\\d,]+)x (.+) was filled!"),
        "sellOfferCancelled" to Regex("\\[Bazaar] Cancelled! Refunded ([\\d,]+)x (.+) from cancelling Sell Offer!"),
        "sellOrderClaimed" to Regex("\\[Bazaar] Claimed ([\\d,]+)x (.+) worth ([\\d,]+) coins sold for ([\\d,]+) each!")
    )

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            var cancelled = false

            patterns["instaBuy"]?.find(text)?.let {
                bzMessage("§a§lInsta-Bought! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["buyOrderSetup"]?.find(text)?.let {
                bzMessage("§e§lBuy Order Setup! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["buyOrderFilled"]?.find(text)?.let {
                bzMessage("§a§lBuy Order Filled! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r!")
                cancelled = true
            }

            patterns["buyOrderCancelled"]?.find(text)?.let {
                bzMessage("§c§lOrder Cancelled! §rRefunded §6${formatNumber(it.groups[1]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["buyOrderClaimed"]?.find(text)?.let {
                val total = formatNumber(it.groups[3]?.value ?: "")
                val per = formatNumber(it.groups[4]?.value ?: "")
                val each = if (total != per) "(§6${per}§r each)" else ""
                bzMessage("§a§lBuy Order Claimed! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${total}§r coins! ${each}")
                cancelled = true
            }

            patterns["instaSell"]?.find(text)?.let {
                bzMessage("§a§lInsta-Sold! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["sellOfferSetup"]?.find(text)?.let {
                bzMessage("§e§lSell Offer Setup! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${formatNumber(it.groups[3]?.value ?: "")}§r coins!")
                cancelled = true
            }

            patterns["sellOfferFilled"]?.find(text)?.let {
                bzMessage("§a§lSell Offer Filled! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r!")
                cancelled = true
            }

            patterns["sellOfferCancelled"]?.find(text)?.let {
                bzMessage("§c§lOrder Cancelled! §rRefunded §c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r!")
                cancelled = true
            }

            patterns["sellOrderClaimed"]?.find(text)?.let {
                val total = formatNumber(it.groups[3]?.value ?: "")
                val per = formatNumber(it.groups[4]?.value ?: "")
                val each = if (total != per) "(§6${per}§r each)" else ""
                bzMessage("§a§lSell Order Claimed! §r§c${it.groups[1]?.value}x §c${clean(it.groups[2]?.value ?: "")}§r for §6${total}§r coins! ${each}")
                cancelled = true
            }

            if (cancelled) event.cancel()
        }
    }

    private fun clean(item: String) = item.replace("ENCHANTED_", "").replace("_", " ").trim()
    private fun bzMessage(message: String) = addMessage("§6[BZ] §r$message")
}