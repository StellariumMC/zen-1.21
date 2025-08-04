package meowing.zen.features.general

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.utils.NetworkUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import java.awt.Color

@Zen.Module
object ContributorColor {
    private var contributorData: Map<String, ContributorInfo>? = null
    private val textReplacements = Object2ObjectLinkedOpenHashMap<String, Text>()

    data class ContributorInfo(
        val displayName: String,
        val glowColor: Int,
        val highlightColor: List<Int>
    )

    init {
        NetworkUtils.fetchJson<Map<String, Map<String, Any>>>(
            "https://raw.githubusercontent.com/kiwidotzip/zen-data/refs/heads/main/assets/ContributorColor.json",
            onSuccess = { data ->
                contributorData = data.mapValues { (_, info) ->
                    val colorList = (info["highlightColor"] as? List<Int>)
                    val glowColor = if (colorList?.size == 4) {
                        val (r, g, b, a) = colorList
                        Color(r, g, b, a).toColorInt()
                    } else {
                        Color(0, 255, 255, 127).toColorInt()
                    }

                    ContributorInfo(
                        displayName = info["displayName"] as? String ?: "",
                        glowColor = glowColor,
                        highlightColor = if (colorList?.size == 4) colorList else listOf(0, 255, 255, 127)
                    )
                }
                updateTextReplacements()
            },
            onError = {
                contributorData = mapOf(
                    "shikiimori" to ContributorInfo("§dKiwi§r", Color(255, 0, 255, 127).toColorInt(), listOf(255, 0, 255, 127)),
                    "cheattriggers" to ContributorInfo("§cCheater§r", Color(255, 0, 0, 127).toColorInt(), listOf(255, 0, 0, 127)),
                    "Aur0raDye" to ContributorInfo("§5Mango 6 7§r", Color(170, 0, 170, 127).toColorInt(), listOf(170, 0, 170, 127)),
                    "Skyblock_Lobby" to ContributorInfo("§9Skyblock_Lobby§r", Color(85, 85, 255, 127).toColorInt(), listOf(85, 85, 255, 127))
                )
                updateTextReplacements()
            }
        )

        EventBus.register<RenderEvent.EntityGlow> ({ event ->
            contributorData?.get(event.entity.name?.string?.removeFormatting())?.let { info ->
                if (mc.player?.canSee(event.entity) == true) {
                    event.shouldGlow = true
                    event.glowColor = info.glowColor
                }
            }
        })
    }

    private fun updateTextReplacements() {
        textReplacements.clear()
        contributorData?.forEach { (username, info) ->
            textReplacements[username] = Text.literal(info.displayName)
        }
    }

    @JvmStatic
    fun replaceText(text: OrderedText): OrderedText {
        return if (textReplacements.isEmpty()) text else Utils.replaceMultipleEntriesInOrdered(text, textReplacements)
    }

    @JvmStatic
    fun replaceText(text: Text): Text {
        return if (textReplacements.isEmpty()) text else Utils.replaceMultipleEntriesInText(text, textReplacements)
    }
}