package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.utils.NetworkUtils
import net.minecraft.text.Text

@Zen.Module
object ContributorColor {
    private var map: Map<String, String>? = null
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