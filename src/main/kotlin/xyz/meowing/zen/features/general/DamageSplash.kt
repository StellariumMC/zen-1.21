package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.elements.MCColorCode
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.format
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.text.Text
import java.text.DecimalFormat

@Zen.Module
object DamageSplash : Feature("damagesplash", true) {
    enum class DamageType(val displayName: String) {
        CRIT("Crit Hits"),
        OVERLOAD("Overload Hits"),
        FIRE("Fire Hits"),
        NORMAL("Non-Crit Hits")
    }

    private val baseColorCodes = arrayOf("§6", "§c", "§e", "§f", "§a", "§b", "§d", "§9")
    private val allSymbols = setOf('✧', '✯', '⚔', '+', '❤', '♞', '☄', '✷', 'ﬗ')
    private val commaFormatter = DecimalFormat("#,###")

    private val enableRainbow by ConfigDelegate<Boolean>("damagesplashrainbow")
    private val normalDamageColor by ConfigDelegate<MCColorCode>("damagesplashnormalcolor")
    private val criticalDamageColor by ConfigDelegate<MCColorCode>("damagesplashcriticalcolor")
    private val enabledColors by ConfigDelegate<Set<Int>>("damagesplashcolors")
    private val showFormatted by ConfigDelegate<Boolean>("damagesplashformatted")
    private val useCommas by ConfigDelegate<Boolean>("damagesplashcommas")
    private val cancelTypes by ConfigDelegate<Set<Int>>("damagesplashcancel")
    private val cancelAll by ConfigDelegate<Boolean>("damagesplashcancelall")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Damage Splash", "", "General", xyz.meowing.zen.ui.ConfigElement(
                "damagesplash",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Show formatted numbers", "Show formatted numbers", "Display", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashformatted",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Use comma separators", "Use comma separators", "Display", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashcommas",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Cancel all damage splash", "Cancel all damage splash", "Cancellation", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashcancelall",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Cancel specific damage types", "Cancel specific damage types", "Cancellation", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashcancel",
                ElementType.MultiCheckbox(
                    options = DamageType.entries.map { it.displayName },
                    default = setOf()
                )
            ))
            .addFeatureOption("Rainbow damage with symbols", "Rainbow damage with symbols", "Colors", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashrainbow",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Normal damage color", "Normal damage color", "Colors", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashnormalcolor",
                ElementType.MCColorPicker(MCColorCode.AQUA)
            ))
            .addFeatureOption("Symbol damage color", "Symbol damage color", "Colors", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashcriticalcolor",
                ElementType.MCColorPicker(MCColorCode.WHITE)
            ))
            .addFeatureOption("Rainbow colors to use", "Rainbow colors to use", "Colors", xyz.meowing.zen.ui.ConfigElement(
                "damagesplashcolors",
                ElementType.MultiCheckbox(
                    options = listOf("Gold", "Red", "Yellow", "White", "Green", "Aqua", "Light Purple", "Blue"),
                    default = setOf(0, 1, 2, 3)
                )
            ))
        return configUI
    }

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
                useCommas -> commaFormatter.format(event.damage)
                else -> event.damage.toString()
            }

            val newName = when {
                hasSymbols && enableRainbow -> addRandomColorCodes(detectedSymbols + formattedDamage + detectedSymbols)
                hasSymbols -> "${criticalDamageColor.code}${detectedSymbols}$formattedDamage${detectedSymbols}"
                else -> "${normalDamageColor.code}$formattedDamage"
            }

            event.entity.customName = Text.literal(newName)
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