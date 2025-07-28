package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.utils.NetworkUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.text.Text
import java.awt.Color

@Zen.Module
object ContributorColor {
    private var contributorData: Map<String, ContributorInfo>? = null
    private val color = "§[0-9a-fklmnor]".toRegex()

    data class ContributorInfo(
        val displayName: String,
        val glowColor: Int
    )

    init {
        NetworkUtils.fetchJson<Map<String, Map<String, Any>>>(
            "https://raw.githubusercontent.com/kiwidotzip/zen-data/refs/heads/main/assets/ContributorColor.json",
            onSuccess = { data ->
                contributorData = data.mapValues { (_, info) ->
                    val colorList = (info["glowColor"] as? List<*>)?.mapNotNull { it as? Int }
                    val glowColor = if (colorList?.size == 4) {
                        val (r, g, b, a) = colorList
                        Color(r, g, b, a).toColorInt()
                    } else {
                        Color(0, 255, 255, 127).toColorInt()
                    }

                    ContributorInfo(
                        displayName = info["displayName"] as? String ?: "",
                        glowColor = glowColor
                    )
                }
            },
            onError = {
                contributorData = mapOf(
                    "shikiimori" to ContributorInfo("§dKiwi§r", Color(255, 0, 255, 127).toColorInt()),
                    "cheattriggers" to ContributorInfo("§cCheater§r", Color(255, 0, 0, 127).toColorInt()),
                    "Aur0raDye" to ContributorInfo("§5Mango 6 7§r", Color(170, 0, 170, 127).toColorInt()),
                    "Skyblock_Lobby" to ContributorInfo("§9Skyblock_Lobby§r", Color(170, 0, 170, 127).toColorInt())
                )
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

    @JvmStatic
    fun replace(text: Text?): Text? {
        if (text == null || contributorData == null) return text

        val original = text.string
        var result = original

        contributorData!!.entries.forEach { (key, info) ->
            val regex = "\\b$key\\b".toRegex()
            result = regex.replace(result) { match ->
                val lastColor = color.findAll(original.substring(0, match.range.first)).lastOrNull()?.value

                when {
                    lastColor != null && !info.displayName.contains("§") -> "$lastColor${info.displayName}"
                    else -> info.displayName.replace("§r", lastColor ?: "§r")
                }
            }
        }

        return if (result != original) Text.literal(result) else text
    }
}