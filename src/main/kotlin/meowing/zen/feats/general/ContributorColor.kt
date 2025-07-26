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
    private var map: Map<String, String>? = null
    private val glowColor = Color(0, 255, 255, 127).toColorInt()
    private val color = "§[0-9a-fklmnor]".toRegex()

    init {
        NetworkUtils.fetchJson<Map<String, String>>(
            "https://raw.githubusercontent.com/kiwidotzip/zen-data/refs/heads/main/assets/ContributorColor.json",
            onSuccess = {
                map = it
            },
            onError = {
                map = mapOf(
                    "shikiimori" to "§dKiwi§r",
                    "cheattriggers" to "§cCheater§r",
                    "Aur0raDye" to "§5Mango 6 7"
                )
            }
        )

        EventBus.register<RenderEvent.EntityGlow> ({ event ->
            if (mc.player?.canSee(event.entity) == true && map?.containsKey(event.entity.name?.string?.removeFormatting()) == true) {
                event.shouldGlow = true
                event.glowColor = glowColor
            }
        })
    }

    @JvmStatic
    fun replace(text: Text?): Text? {
        if (text == null || map == null) return text

        val original = text.string
        var result = original

        map!!.entries.forEach { (key, value) ->
            val regex = "\\b$key\\b".toRegex()
            result = regex.replace(result) { match ->
                val lastColor = color.findAll(original.substring(0, match.range.first)).lastOrNull()?.value

                when {
                    lastColor != null && !value.contains("§") -> "$lastColor$value"
                    else -> value.replace("§r", lastColor ?: "§r")
                }
            }
        }

        return if (result != original) Text.literal(result) else text
    }
}