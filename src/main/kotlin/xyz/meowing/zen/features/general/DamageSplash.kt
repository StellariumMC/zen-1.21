package xyz.meowing.zen.features.general

import xyz.meowing.zen.config.ui.elements.MCColorCode
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.format
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.chat.Component
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.SkyblockEvent
import java.text.DecimalFormat

@Module
object DamageSplash : Feature(
    "damageSplash",
    "Damage splash",
    "Customize damage numbers that appear above mobs",
    "General",
    skyblockOnly = true
) {
    enum class DamageType(val displayName: String) {
        CRIT("Crit Hits"),
        OVERLOAD("Overload Hits"),
        FIRE("Fire Hits"),
        NORMAL("Non-Crit Hits")
    }

    private val baseColorCodes = arrayOf("§6", "§c", "§e", "§f", "§a", "§b", "§d", "§9")
    private val allSymbols = setOf('✧', '✯', '⚔', '+', '❤', '♞', '☄', '✷', 'ﬗ')
    private val commaFormatter = DecimalFormat("#,###")

    private val showFormatted by config.switch("Show formatted numbers", true)
    private val enableRainbow by config.switch("Rainbow text", true)
    private val normalDamageColor by config.mcColorPicker("Normal damage color", MCColorCode.AQUA)
    private val criticalDamageColor by config.mcColorPicker("Crit damage color", MCColorCode.RED)
    private val enabledColors by config.multiCheckbox("Rainbow colors to use", listOf("Gold", "Red", "Yellow", "White", "Green", "Aqua", "Light Purple", "Blue"), setOf(0, 1, 2, 3))
    private val commas by config.switch("Use commas", true)
    private val cancelTypes by config.multiCheckbox("Filtered types", DamageType.entries.map { it.displayName })
    private val cancelAll by config.switch("Cancel all types", false)

    override fun initialize() {
        register<SkyblockEvent.DamageSplash>(priority = 1000) { event ->
            if (cancelAll) return@register event.cancel()

            val name = event.originalName.removeFormatting()
            val damageType = detectDamageType(event.originalName, name)

            if (cancelTypes.contains(damageType.ordinal)) return@register event.cancel()

            val detectedSymbols = name.filter { it in allSymbols }.toSet().joinToString("")
            val hasSymbols = detectedSymbols.isNotEmpty()

            val formattedDamage = when {
                showFormatted -> format(event.damage)
                commas -> commaFormatter.format(event.damage)
                else -> event.damage.toString()
            }

            val newName = when {
                hasSymbols && enableRainbow -> addRandomColorCodes(detectedSymbols + formattedDamage + detectedSymbols)
                hasSymbols -> "${criticalDamageColor.code}${detectedSymbols}$formattedDamage${detectedSymbols}"
                else -> "${normalDamageColor.code}$formattedDamage"
            }

            event.entity.customName = Component.literal(newName)
        }
    }

    private fun detectDamageType(originalName: String, cleanName: String): DamageType {
        return when {
            cleanName.contains("✧") -> DamageType.CRIT
            cleanName.contains("✯") -> DamageType.OVERLOAD
            originalName.contains("§6") -> DamageType.FIRE
            else -> DamageType.NORMAL
        }
    }

    private fun addRandomColorCodes(inputString: String): String {
        val cleanString = inputString.removeFormatting()
        if (cleanString.isEmpty()) return inputString

        val activeColors = enabledColors.map { baseColorCodes[it] }.toTypedArray()
        if (activeColors.isEmpty()) return inputString

        return buildString(cleanString.length * 6) {
            var lastColorIndex = -1

            for (char in cleanString) {
                var colorIndex: Int
                do colorIndex = activeColors.indices.random() while (colorIndex == lastColorIndex && activeColors.size > 1)

                append(activeColors[colorIndex])
                append(char)
                append("§r")
                lastColorIndex = colorIndex
            }
        }
    }
}