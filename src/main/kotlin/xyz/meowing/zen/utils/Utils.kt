package xyz.meowing.zen.utils

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.sounds.SoundEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FormattedCharSequence
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.input.KnitInputs
import java.awt.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Optional

object Utils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()
    private val formatRegex = "[§&][0-9a-fk-or]".toRegex()
    private val suffixes = arrayOf(
        1000L to "k",
        1000000L to "m",
        1000000000L to "b",
        1000000000000L to "t",
        1000000000000000L to "p",
        1000000000000000000L to "e"
    )

    inline val partialTicks get() = client.deltaTracker.getGameTimeDeltaPartialTick(true)
    inline val window get() = client.window

    fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        Minecraft.getInstance().player?.playSound(sound, volume, pitch)
    }

    fun getKeyName(keyCode: Int): String {
        return when {
            keyCode == 0 -> "None"
            keyCode < 0 -> {
                when (val button = -keyCode - 1) {
                    0 -> "LMB"
                    1 -> "RMB"
                    2 -> "MMB"
                    else -> "Mouse ${button + 1}"
                }
            }
            else -> KnitInputs.getDisplayName(keyCode)
        }
    }

    fun String?.removeFormatting(): String {
        if (this == null) return ""
        return this.replace(formatRegex, "")
    }

    fun String.getRegexGroups(regex: Regex): MatchGroupCollection? {
        val regexMatchResult = regex.find(this) ?: return null
        return regexMatchResult.groups
    }

    fun format(value: Number): String {
        val longValue = value.toLong()

        when {
            longValue == Long.MIN_VALUE -> return format(Long.MIN_VALUE + 1)
            longValue < 0L -> return "-${format(-longValue)}"
            longValue < 1000L -> return longValue.toString()
        }

        val (threshold, suffix) = suffixes.findLast { longValue >= it.first } ?: return longValue.toString()
        val scaled = longValue * 10 / threshold

        return if (scaled < 100 && scaled % 10 != 0L) "${scaled / 10.0}$suffix" else "${scaled / 10}$suffix"
    }

    inline val AbstractContainerScreen<*>.chestName: String get() {
        val screenHandler = this.menu
        if (screenHandler !is ChestMenu) return ""
        return this.title.string.trim()
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun Int.toColorFloat(): Float {
        return this / 255f
    }

    fun Color.toFloatArray(): FloatArray {
        return floatArrayOf(red / 255f, green / 255f, blue / 255f)
    }

    fun Map<*, *>.toColorFromMap(): Color? {
        return try {
            val r = (get("r") as? Number)?.toInt() ?: 255
            val g = (get("g") as? Number)?.toInt() ?: 255
            val b = (get("b") as? Number)?.toInt() ?: 255
            val a = (get("a") as? Number)?.toInt() ?: 255
            Color(r, g, b, a)
        } catch (e: Exception) {
            null
        }
    }

    fun List<*>.toColorFromList(): Color? {
        return try {
            if (size < 4) return null
            Color(
                (this[0] as? Number)?.toInt() ?: return null,
                (this[1] as? Number)?.toInt() ?: return null,
                (this[2] as? Number)?.toInt() ?: return null,
                (this[3] as? Number)?.toInt() ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }

    /*
     * Modified code from Aaron's Mod
     * https://github.com/AzureAaron/aaron-mod/blob/master/src/main/java/net/azureaaron/mod/utils/TextTransformer.java
     */
    fun replaceMultipleEntriesInText(text: Component, replacements: Object2ObjectLinkedOpenHashMap<String, Component>): Component {
        if (replacements.isEmpty())
            return text

        val content = text.string
        val contentLength = content.length

        if (contentLength == 0)
            return text

        data class ReplacementEntry(val target: String, val replacement: Component, val targetLen: Int)

        val entries = ArrayList<ReplacementEntry>(replacements.size)
        var hasAnyMatch = false

        for ((target, replacement) in replacements) {
            if (!hasAnyMatch && content.indexOf(target) != -1) {
                hasAnyMatch = true
            }
            entries.add(ReplacementEntry(target, replacement, target.length))
        }

        if (!hasAnyMatch)
            return text

        entries.sortByDescending { it.targetLen }

        data class StyledChar(val char: Char, val style: Style)
        val styledChars = ArrayList<StyledChar>(contentLength)

        text.visit({ style, str ->
            for (char in str) {
                styledChars.add(StyledChar(char, style))
            }
            Optional.empty<Any>()
        }, Style.EMPTY)

        val result = Component.empty() as MutableComponent
        var idx = 0

        while (idx < contentLength && idx < styledChars.size) {
            var found = false

            for (i in 0 until entries.size) {
                val entry = entries[i]
                if (idx + entry.targetLen <= contentLength &&
                    content.regionMatches(idx, entry.target, 0, entry.targetLen)) {
                    result.append(entry.replacement)
                    idx += entry.targetLen
                    found = true
                    break
                }
            }

            if (!found) {
                val styledChar = styledChars[idx]
                result.append(Component.literal(String(Character.toChars(content.codePointAt(idx)))).setStyle(styledChar.style))
                idx++
            }

        }

        return result
    }

    fun replaceMultipleEntriesInOrdered(orderedText: FormattedCharSequence, replacements: Object2ObjectLinkedOpenHashMap<String, Component>): FormattedCharSequence {
        if (replacements.isEmpty())
            return orderedText

        val textBuilder = StringBuilder()
        val styles = ArrayList<Pair<Int, Style>>()

        orderedText.accept { _, style, codePoint ->
            styles.add(textBuilder.length to style)
            textBuilder.appendCodePoint(codePoint)
            true
        }

        val content = textBuilder.toString()
        val contentLength = content.length

        if (contentLength == 0)
            return orderedText

        data class ReplacementEntry(val target: String, val replacement: Component, val targetLen: Int)

        val entries = ArrayList<ReplacementEntry>(replacements.size)
        var hasAnyMatch = false

        for ((target, replacement) in replacements) {
            if (!hasAnyMatch && content.indexOf(target) != -1) {
                hasAnyMatch = true
            }
            entries.add(ReplacementEntry(target, replacement, target.length))
        }

        if (!hasAnyMatch)
            return orderedText

        entries.sortByDescending { it.targetLen }

        val styleMap = HashMap<Int, Style>(styles.size)
        for (i in 0 until styles.size) {
            styleMap[styles[i].first] = styles[i].second
        }

        val result = Component.empty() as MutableComponent
        var idx = 0

        while (idx < contentLength) {
            var found = false

            for (i in 0 until entries.size) {
                val entry = entries[i]
                if (idx + entry.targetLen <= contentLength &&
                    content.regionMatches(idx, entry.target, 0, entry.targetLen)) {
                    result.append(entry.replacement)
                    idx += entry.targetLen
                    found = true
                    break
                }
            }

            if (!found) {
                val style = styleMap[idx] ?: Style.EMPTY
                result.append(Component.literal(String(Character.toChars(content.codePointAt(idx)))).setStyle(style))
                idx++
            }
        }

        return result.visualOrderText
    }

    fun Component.toLegacyString(): String {
        val sb = StringBuilder()
        this.visit({ style, text ->
            style.color?.let { color ->
                ChatFormatting.entries.firstOrNull {
                    it.color == color.value
                }?.let { sb.append('§').append(it.char) }
            }
            if (style.isBold) sb.append("§l")
            if (style.isItalic) sb.append("§o")
            if (style.isUnderlined) sb.append("§n")
            if (style.isStrikethrough) sb.append("§m")
            if (style.isObfuscated) sb.append("§k")
            sb.append(text)
            Optional.empty<Unit>()
        }, Style.EMPTY)
        return sb.toString()
    }

    fun getFormattedDate(): String {
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val suffix = getDaySuffix(day)
        val formatter = DateTimeFormatter.ofPattern("MMMM d'$suffix', yyyy", Locale.ENGLISH)
        return today.format(formatter)
    }

    private fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    fun getPlayerTexture(
        playerUuid: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        NetworkUtils.getJson(
            url = "https://sessionserver.mojang.com/session/minecraft/profile/$playerUuid",
            onSuccess = { json ->
                val properties = json.getAsJsonArray("properties")
                properties?.forEach { element ->
                    val property = element.asJsonObject
                    if (property.get("name")?.asString == "textures") {
                        val texture = property.get("value")?.asString
                        if (texture != null) {
                            onSuccess(texture)
                            return@getJson
                        }
                    }
                }
                onError(IllegalArgumentException("No texture found for player UUID: $playerUuid"))
            },
            onError = onError
        )
    }

    fun getPlayerUuid(
        playerName: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        NetworkUtils.getJson(
            url = "https://api.mojang.com/users/profiles/minecraft/$playerName",
            onSuccess = { json ->
                val id = json.get("id")?.asString
                if (id != null) {
                    onSuccess(id)
                } else {
                    onError(IllegalArgumentException("No UUID found for player: $playerName"))
                }
            },
            onError = onError
        )
    }
}