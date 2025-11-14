package xyz.meowing.zen.features.general

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.MCColorCode
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.format
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.chat.Component
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.text.DecimalFormat

@Module
object DamageSplash : Feature(
    "damagesplash",
    true
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

    private val enableRainbow by ConfigDelegate<Boolean>("damageSplash.rainbow")
    private val normalDamageColor by ConfigDelegate<MCColorCode>("damageSplash.normalColor")
    private val criticalDamageColor by ConfigDelegate<MCColorCode>("damageSplash.criticalColor")
    private val enabledColors by ConfigDelegate<Set<Int>>("damageSplash.rainbowColors")
    private val showFormatted by ConfigDelegate<Boolean>("damageSplash.formatted")
    private val useCommas by ConfigDelegate<Boolean>("damageSplash.commas")
    private val cancelTypes by ConfigDelegate<Set<Int>>("damageSplash.cancelTypes")
    private val cancelAll by ConfigDelegate<Boolean>("damageSplash.cancelAll")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Damage splash",
                "Customize damage numbers that appear above mobs",
                "General",
                ConfigElement(
                    "damageSplash",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Show formatted numbers",
                ConfigElement(
                    "damageSplash.formatted",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Use comma separators",
                ConfigElement(
                    "damageSplash.commas",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Cancel all damage splash",
                ConfigElement(
                    "damageSplash.cancelAll",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Cancel specific damage types",
                ConfigElement(
                    "damageSplash.cancelTypes",
                    ElementType.MultiCheckbox(
                        options = DamageType.entries.map { it.displayName },
                        default = setOf()
                    )
                )
            )
            .addFeatureOption(
                "Rainbow damage with symbols",
                ConfigElement(
                    "damageSplash.rainbow",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Normal damage color",
                ConfigElement(
                    "damageSplash.normalColor",
                    ElementType.MCColorPicker(MCColorCode.AQUA)
                )
            )
            .addFeatureOption(
                "Symbol damage color",
                ConfigElement(
                    "damageSplash.criticalColor",
                    ElementType.MCColorPicker(MCColorCode.WHITE)
                )
            )
            .addFeatureOption(
                "Rainbow colors to use",
                ConfigElement(
                    "damageSplash.rainbowColors",
                    ElementType.MultiCheckbox(
                        options = listOf("Gold", "Red", "Yellow", "White", "Green", "Aqua", "Light Purple", "Blue"),
                        default = setOf(0, 1, 2, 3)
                    )
                )
            )
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