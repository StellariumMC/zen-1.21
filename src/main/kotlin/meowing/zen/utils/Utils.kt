package meowing.zen.utils

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import meowing.zen.Zen.Companion.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.sound.SoundEvent
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.joml.Matrix3x2fStack
import java.awt.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    inline val partialTicks get() = mc.renderTickCounter.getTickProgress(true)
    inline val window get() = mc.window
    inline val MouseX get() = mc.mouse.x * window.scaledWidth / window.width
    inline val MouseY get() = mc.mouse.y * window.scaledWidth / window.width

    fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        MinecraftClient.getInstance().player?.playSound(sound, volume, pitch)
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

    inline val HandledScreen<*>.chestName: String get() {
        val screenHandler = this.screenHandler
        if (screenHandler !is GenericContainerScreenHandler) return ""
        return this.title.string.trim()
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun Color.toColorInt(): Int {
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

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

    fun decodeRoman(roman: String): Int {
        val values = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var result = 0
        var prev = 0

        for (char in roman.reversed()) {
            val current = values[char] ?: 0
            if (current < prev) result -= current
            else result += current
            prev = current
        }
        return result
    }

    fun Long.toFormattedDuration(short: Boolean = false): String {
        val seconds = this / 1000
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        if (short) {
            return when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${remainingSeconds}s"
            }
        }

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (remainingSeconds > 0) append("${remainingSeconds}s")
        }.trimEnd()
    }

    fun createBlock(radius: Float = 0f): UIComponent {
        return if (SystemUtils.IS_OS_MAC_OSX) UIBlock() else UIRoundedRectangle(radius)
    }

    /*
     * Modified code from Aaron's Mod
     * https://github.com/AzureAaron/aaron-mod/blob/master/src/main/java/net/azureaaron/mod/utils/TextTransformer.java
     */
    fun replaceMultipleEntriesInText(text: Text, replacements: Object2ObjectLinkedOpenHashMap<String, Text>): Text {
        val stringified = text.string
        var newText = text as? MutableText ?: Text.empty().append(text) as MutableText

        for ((wantedWord, replacementText) in replacements) {
            val occurs = stringified.indexOf(wantedWord) != -1
            if (!occurs) continue

            val occurrences = StringUtils.countMatches(stringified, wantedWord)
            var indexFrom = 0

            repeat(occurrences) {
                val currentString = newText.string
                val startIndex = currentString.indexOf(wantedWord, indexFrom)
                val endIndex = startIndex + wantedWord.length

                if (startIndex == -1) return@repeat

                val textComponents = newText.siblings

                if (textComponents.size <= startIndex) {
                    newText = deconstructAllComponents(newText)
                }

                val components = newText.siblings
                if (components.size > startIndex) {
                    components[startIndex] = replacementText

                    for (i in endIndex - 1 downTo startIndex + 1) {
                        if (i < components.size) {
                            components.removeAt(i)
                        }
                    }
                }

                newText = deconstructComponents(newText)

                val lengthDiff = newText.string.length - currentString.length
                indexFrom = endIndex + lengthDiff
            }
        }

        return newText
    }

    fun replaceMultipleEntriesInOrdered(orderedText: OrderedText, replacements: Object2ObjectLinkedOpenHashMap<String, Text>): OrderedText {
        val text = orderedTextToText(orderedText)
        return replaceMultipleEntriesInText(text, replacements).asOrderedText()
    }

    private fun deconstructComponents(text: Text): MutableText {
        val currentComponents = text.siblings
        val newText = Text.empty() as MutableText
        val newComponents = newText.siblings

        for (current in currentComponents) {
            val currentString = current.string

            if (currentString.length <= 1) {
                newComponents.add(current)
                continue
            }

            current.asOrderedText().accept { _, style, codePoint ->
                newComponents.add(Text.literal(Character.toString(codePoint)).setStyle(style))
                true
            }
        }

        return newText
    }

    private fun deconstructAllComponents(text: Text): MutableText {
        val newText = Text.empty() as MutableText
        val newComponents = newText.siblings

        text.asOrderedText().accept { _, style, codePoint ->
            newComponents.add(Text.literal(Character.toString(codePoint)).setStyle(style))
            true
        }

        for (sibling in text.siblings) {
            sibling.asOrderedText().accept { _, style, codePoint ->
                newComponents.add(Text.literal(Character.toString(codePoint)).setStyle(style))
                true
            }
        }

        return newText
    }

    private fun orderedTextToText(orderedText: OrderedText): Text {
        val text = Text.empty() as MutableText

        orderedText.accept { _, style, codePoint ->
            text.append(Text.literal(Character.toString(codePoint)).setStyle(style))
            true
        }

        return text
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
                val properties = json["properties"]?.jsonArray
                properties?.forEach { element ->
                    val property = element.jsonObject
                    if (property["name"]?.jsonPrimitive?.content == "textures") {
                        property["value"]?.jsonPrimitive?.content?.let { onSuccess(it) }
                        return@getJson
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
                json["id"]?.jsonPrimitive?.content?.let { onSuccess(it) } ?: onError(IllegalArgumentException("No UUID found for player: $playerName"))
            },
            onError = onError
        )
    }

    fun Matrix3x2fStack.pop() {
        this.popMatrix()
    }

    fun Matrix3x2fStack.push() {
        this.pushMatrix()
    }
}