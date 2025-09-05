package meowing.zen.features.general

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import meowing.zen.Zen
import meowing.zen.api.PlayerStats
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.elements.MCColorCode
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GameEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.configRegister
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Render2D.width
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.awt.Color

@Zen.Module
object StatsDisplay : Feature("statsdisplay") {
    private const val healthBarName = "Health Bar"
    private const val manaBarName = "Mana Bar"
    private const val overflowManaName = "Overflow Mana"
    private const val riftTimeBarName = "Rift Time Bar"
    private const val drillFuelBarName = "Drill Fuel Bar"
    private const val PROCESSED_MARKER = "§z§e§b"

    private var initedShadersUI = false

    private val hiddenstats by ConfigDelegate<Set<Int>>("hiddenstats")
    private val hideVanillaHp by ConfigDelegate<Boolean>("hidevanillahp")
    private val hideVanillaArmor by ConfigDelegate<Boolean>("hidevanillaarmor")
    private val hideExpBar by ConfigDelegate<Boolean>("hideexpbar")

    @JvmStatic fun shouldHideVanillaHealth(): Boolean = isEnabled() && hideVanillaHp
    @JvmStatic fun shouldHideVanillaArmor(): Boolean = isEnabled() && hideVanillaArmor
    @JvmStatic fun shouldHideExpBar(): Boolean = isEnabled() && hideExpBar

    private val showHealthBar by ConfigDelegate<Boolean>("showhealthbar")
    private val healthBarFillColor by ConfigDelegate<Color>("healthbarmaincolor")
    private val healthBarExtraColor by ConfigDelegate<Color>("healthbarextracolor")
    private val healthtextstyle by ConfigDelegate<Int>("healthtextstyle")
    private val showHealthText by ConfigDelegate<Boolean>("showhealthtext")
    private val showMaxHealth by ConfigDelegate<Boolean>("showmaxhealth")
    private val healthTextColor by ConfigDelegate<MCColorCode>("healthtextcolor")
    private val maxHealthTextColor by ConfigDelegate<MCColorCode>("maxhealthtextcolor")

    private val showManaBar by ConfigDelegate<Boolean>("showmanabar")
    private val manaBarFillColor by ConfigDelegate<Color>("manabarmaincolor")
    private val manatextstyle by ConfigDelegate<Int>("manatextstyle")
    private val showManaText by ConfigDelegate<Boolean>("showmanatext")
    private val showMaxMana by ConfigDelegate<Boolean>("showmaxmana")
    private val manaTextColor by ConfigDelegate<MCColorCode>("manatextcolor")
    private val maxManaTextColor by ConfigDelegate<MCColorCode>("maxmanatextcolor")

    private val showOverflowManaText by ConfigDelegate<Boolean>("showoverflowmanatext")
    private val overflowManaTextColor by ConfigDelegate<MCColorCode>("overflowmanatextcolor")

    private val showRiftTimeText by ConfigDelegate<Boolean>("showrifttimetext")
    private val riftTimeTextColor by ConfigDelegate<MCColorCode>("rifttimetextcolor")

    private val showDrillFuelText by ConfigDelegate<Boolean>("showdrillfueltext")
    private val showMaxDrillFuel by ConfigDelegate<Boolean>("showmaxdrillfuel")
    private val drillFuelTextColor by ConfigDelegate<MCColorCode>("drillfueltextcolor")
    private val maxDrillFuelTextColor by ConfigDelegate<MCColorCode>("maxdrillfueltextcolor")

    private enum class StatType(val displayName: String, val regex: Regex) {
        HEALTH("Health", """(§.)(?<currentHealth>[\d,]+)/(?<maxHealth>[\d,]+)❤""".toRegex()),
        MANA("Mana", """§b(?<currentMana>[\d,]+)/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()),
        OVERFLOW_MANA("Overflow Mana", """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()),
        DEFENSE("Defense", """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()),
        RIFT_TIME("Rift Time", """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()),
        DRILL_FUEL("Drill Fuel", """§2(?<currentFuel>[\d,]+)/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()),
    }

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Stats Display", ConfigElement(
                "statsdisplay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "cleanactionbar",
                "Clean Action Bar",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hidevanillahp",
                "Hide Vanilla HP and Saturation",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hidevanillaarmor",
                "Hide Armor Icon",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hideexpbar",
                "Hide Experience Bar",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hiddenstats",
                "Hide Stats",
                ElementType.MultiCheckbox(
                    options = StatType.entries.map { it.displayName },
                    default = emptySet()
                )
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "showhealthbar",
                "Show Health Bar",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "healthbarmaincolor",
                "Health Bar Fill Color",
                ElementType.ColorPicker(MCColorCode.RED.color)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "healthbarextracolor",
                "Health Bar Absorption Fill Color",
                ElementType.ColorPicker(MCColorCode.YELLOW.color)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "showhealthtext",
                "Show Health Numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "healthtextstyle",
                "Health Text Style",
                ElementType.Dropdown(listOf("Shadow", "Default", "Outline"), 0)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "showmaxhealth",
                "Show Max Health",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "healthtextcolor",
                "Health Text Color",
                ElementType.MCColorPicker(MCColorCode.RED)
            ))
            .addElement("General", "Stats Display", "Health Display", ConfigElement(
                "maxhealthtextcolor",
                "Max Health Text Color",
                ElementType.MCColorPicker(MCColorCode.RED)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "showmanabar",
                "Show Mana Bar",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "manabarmaincolor",
                "Health Bar Fill Color",
                ElementType.ColorPicker(MCColorCode.BLUE.color)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "showmanatext",
                "Show Mana Numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "manatextstyle",
                "Mana Text Style",
                ElementType.Dropdown(listOf("Shadow", "Default", "Outline"), 0)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "showmaxmana",
                "Show Max Mana",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "manatextcolor",
                "Mana Text Color",
                ElementType.MCColorPicker(MCColorCode.BLUE)
            ))
            .addElement("General", "Stats Display", "Mana Display", ConfigElement(
                "maxmanatextcolor",
                "Max Mana Text Color",
                ElementType.MCColorPicker(MCColorCode.BLUE)
            ))
            .addElement("General", "Stats Display", "Overflow Mana", ConfigElement(
                "showoverflowmanatext",
                "Show Overflow Mana",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Overflow Mana", ConfigElement(
                "overflowmanatextcolor",
                "Overflow Mana Text Color",
                ElementType.MCColorPicker(MCColorCode.DARK_AQUA)
            ))
            .addElement("General", "Stats Display", "Rift Time Bar", ConfigElement(
                "showrifttimetext",
                "Show Rift Time Text",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Rift Time Bar", ConfigElement(
                "rifttimetextcolor",
                "Rift Time Text Color",
                ElementType.MCColorPicker(MCColorCode.GREEN)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "showdrillfueltext",
                "Show Drill Fuel Numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "showmaxdrillfuel",
                "Show Max Drill Fuel",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "drillfueltextcolor",
                "Drill Fuel Text Color",
                ElementType.MCColorPicker(MCColorCode.DARK_GREEN)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "maxdrillfueltextcolor",
                "Max Drill Fuel Text Color",
                ElementType.MCColorPicker(MCColorCode.GREEN)
            ))
    }

    override fun initialize() {
        HUDManager.register(overflowManaName, "§3642ʬ")
        HUDManager.registerCustom(healthBarName, 80, 10, this::healthBarEditorRender)
        HUDManager.registerCustom(manaBarName, 80, 10, this::manaBarEditorRender)
        HUDManager.registerCustom(riftTimeBarName, 80, 10, this::riftTimeBarEditorRender)
        HUDManager.registerCustom(drillFuelBarName, 80, 10, this::drillFuelBarEditorRender)

        configRegister<GameEvent.ActionBar>(listOf("statsdisplay", "cleanactionbar"), priority = 1000) { event ->
            val originalText = event.message.string

            if (originalText.endsWith(PROCESSED_MARKER)) return@configRegister

            val cleanedText = hiddenstats.fold(originalText) { text, index ->
                StatType.entries.getOrNull(index)?.regex?.replace(text, "") ?: text
            }.trim().replace("§r  ", " ")

            if (cleanedText != originalText) {
                event.cancel()
                player?.sendMessage(Text.literal(cleanedText + PROCESSED_MARKER), true)
            }
        }

        register<RenderEvent.HUD> { event ->
            renderHealthBar(event.context)
            renderManaBar(event.context)
            renderOverflowMana(event.context)
            renderRiftTimeBar(event.context)
            renderDrillFuelBar(event.context)
        }
    }

    private fun renderBar(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, primaryFill: Double, primaryColor: Color, secondaryFill: Double = 0.0, secondaryColor: Color? = null) {
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val borderWidth = 1f * scale
        val fillHeight = 8f * scale

        //#if MC >= 1.21.6
        //$$ context.fill(
        //$$     x.toInt(), y.toInt(),
        //$$     (x + scaledWidth).toInt(), (y + scaledHeight).toInt(),
        //$$     (Color.BLACK.alpha shl 24) or (Color.BLACK.red shl 16) or (Color.BLACK.green shl 8) or Color.BLACK.blue
        //$$ )
        //$$ context.fill(
        //$$     (x + borderWidth).toInt(), (y + borderWidth).toInt(),
        //$$     (x + scaledWidth - borderWidth).toInt(), (y + borderWidth + fillHeight).toInt(),
        //$$     (Color.DARK_GRAY.alpha shl 24) or (Color.DARK_GRAY.red shl 16) or (Color.DARK_GRAY.green shl 8) or Color.DARK_GRAY.blue
        //$$ )
        //$$
        //$$ val availableWidth = scaledWidth - 2 * borderWidth
        //$$ val primaryWidth = (availableWidth * primaryFill).toFloat()
        //$$ val secondaryWidth = (availableWidth * secondaryFill).toFloat()
        //$$
        //$$ if (primaryWidth > 0) {
        //$$     context.fill(
        //$$         (x + borderWidth).toInt(), (y + borderWidth).toInt(),
        //$$         (x + borderWidth + primaryWidth).toInt(), (y + borderWidth + fillHeight).toInt(),
        //$$         (primaryColor.alpha shl 24) or (primaryColor.red shl 16) or (primaryColor.green shl 8) or primaryColor.blue
        //$$     )
        //$$ }
        //$$
        //$$ if (secondaryFill > 0 && secondaryColor != null && secondaryWidth > 0) {
        //$$     context.fill(
        //$$         (x + borderWidth + primaryWidth).toInt(), (y + borderWidth).toInt(),
        //$$         (x + borderWidth + primaryWidth + secondaryWidth).toInt(), (y + borderWidth + fillHeight).toInt(),
        //$$         (secondaryColor.alpha shl 24) or (secondaryColor.red shl 16) or (secondaryColor.green shl 8) or secondaryColor.blue
        //$$     )
        //$$ }
        //#else
        val radius = 2f * scale
        if (!initedShadersUI) UIRoundedRectangle.initShaders()
        UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), x, y, x + scaledWidth, y + scaledHeight, radius, Color.BLACK)
        UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), x + borderWidth, y + borderWidth, x + scaledWidth - borderWidth, y + borderWidth + fillHeight, radius * 0.75f, Color.DARK_GRAY)
        val availableWidth = scaledWidth - 2 * borderWidth
        val primaryWidth = (availableWidth * primaryFill).toFloat()
        val secondaryWidth = (availableWidth * secondaryFill).toFloat()
        if (primaryWidth > 0) {
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), x + borderWidth, y + borderWidth, x + borderWidth + primaryWidth, y + borderWidth + fillHeight, radius * 0.75f, primaryColor)
        }
        if (secondaryFill > 0 && secondaryColor != null && secondaryWidth > 0) {
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), x + borderWidth + primaryWidth, y + borderWidth, x + borderWidth + primaryWidth + secondaryWidth, y + borderWidth + fillHeight, radius * 0.75f, secondaryColor)
        }
        //#endif
    }

    private fun renderText(context: DrawContext, text: String, x: Float, y: Float, width: Int, scale: Float, style: Render2D.TextStyle = Render2D.TextStyle.DROP_SHADOW) {
        val textWidth = text.width() * scale
        val scaledWidth = width * scale
        val centerX = x + scaledWidth / 2f
        val textX = centerX - textWidth / 2f
        val textY = if (text.contains("ʬ")) y else y - (8f * scale)
        Render2D.renderString(context, text, textX, textY, scale, textStyle = style)
    }

    private fun renderHealthBar(context: DrawContext) {
        if (!HUDManager.isEnabled(healthBarName) || PlayerStats.maxHealth == 0) return
        val max = PlayerStats.maxHealth
        val absorption = PlayerStats.absorption
        val health = PlayerStats.displayedHealth
        val total = max + absorption
        val healthFillPerc = health.toDouble() / total
        val absorbFillPerc = absorption.toDouble() / total
        val x = HUDManager.getX(healthBarName)
        val y = HUDManager.getY(healthBarName)
        val scale = HUDManager.getScale(healthBarName)

        healthBarEditorRender(context, x, y, 80, 10, scale, 0f, false, healthFillPerc, absorbFillPerc)
    }

    private fun renderManaBar(context: DrawContext) {
        if (!HUDManager.isEnabled(manaBarName) || PlayerStats.maxMana == 0) return
        val max = PlayerStats.maxMana
        val current = PlayerStats.displayedMana
        val fillPerc = current.toDouble() / max
        val x = HUDManager.getX(manaBarName)
        val y = HUDManager.getY(manaBarName)
        val scale = HUDManager.getScale(manaBarName)

        manaBarEditorRender(context, x, y, 80, 10, scale, 0f, false, fillPerc)
    }

    private fun renderOverflowMana(context: DrawContext) {
        if (!HUDManager.isEnabled(overflowManaName)) return
        val x = HUDManager.getX(overflowManaName)
        val y = HUDManager.getY(overflowManaName)
        val scale = HUDManager.getScale(overflowManaName)

        if (showOverflowManaText) {
            val overflowMana = PlayerStats.overflowMana

            if (overflowMana > 0) {
                val overflowText = "${overflowManaTextColor.code}${overflowMana}ʬ"
                renderText(context, overflowText, x, y, overflowText.width(), scale)
            }
        }
    }

    private fun renderRiftTimeBar(context: DrawContext) {
        if (!HUDManager.isEnabled(riftTimeBarName) || PlayerStats.maxRiftTime == 0) return
        val current = PlayerStats.riftTimeSeconds
        val max = PlayerStats.maxRiftTime
        val fillPerc = current.toDouble() / max
        val x = HUDManager.getX(riftTimeBarName)
        val y = HUDManager.getY(riftTimeBarName)
        val scale = HUDManager.getScale(riftTimeBarName)

        riftTimeBarEditorRender(context, x, y, 80, 10, scale, 0f, false, fillPerc)
    }

    private fun renderDrillFuelBar(context: DrawContext) {
        if (!HUDManager.isEnabled(drillFuelBarName) || PlayerStats.maxDrillFuel == 0) return
        val max = PlayerStats.maxDrillFuel
        val current = PlayerStats.drillFuel
        val fillPerc = current.toDouble() / max
        val x = HUDManager.getX(drillFuelBarName)
        val y = HUDManager.getY(drillFuelBarName)
        val scale = HUDManager.getScale(drillFuelBarName)

        drillFuelBarEditorRender(context, x, y, 80, 10, scale, 0f, false, fillPerc)
    }

    fun healthBarEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, healthPerc: Double = 0.75, absorbPerc: Double = 0.25) {
        if(showHealthBar) {
            renderBar(context, x, y, width, height, scale, healthPerc, healthBarFillColor, absorbPerc, healthBarExtraColor)
        }

        if (showHealthText || previewMode) {
            val currentHealth = if (previewMode) {
                (1000 * (healthPerc + absorbPerc)).toInt()
            } else {
                PlayerStats.health + PlayerStats.absorption
            }

            val healthText = if (showMaxHealth) {
                val maxValue = if (previewMode) 1000 else PlayerStats.maxHealth
                "${healthTextColor.code}$currentHealth§8/${maxHealthTextColor.code}$maxValue"
            } else {
                "${healthTextColor.code}$currentHealth"
            }

            val healthTextStyle = when (healthtextstyle) {
                0 -> Render2D.TextStyle.DROP_SHADOW
                1 -> Render2D.TextStyle.DEFAULT
                2 -> Render2D.TextStyle.BLACK_OUTLINE
                else -> Render2D.TextStyle.DROP_SHADOW
            }
            renderText(context, healthText, x, y, width, scale, healthTextStyle)
        }
    }

    fun manaBarEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, manaPerc: Double = 0.6) {
        if(showManaBar) {
            renderBar(context, x, y, width, height, scale, manaPerc, manaBarFillColor)
        }

        if (showManaText || previewMode) {
            val currentMana = if (previewMode) {
                (1000 * manaPerc).toInt()
            } else {
                PlayerStats.mana
            }

            val manaText = if (showMaxMana) {
                val maxValue = if (previewMode) 1000 else PlayerStats.maxMana
                "${manaTextColor.code}$currentMana§8/${maxManaTextColor.code}$maxValue"
            } else {
                "${manaTextColor.code}$currentMana"
            }

            val manaTextStyle = when (manatextstyle) {
                0 -> Render2D.TextStyle.DROP_SHADOW
                1 -> Render2D.TextStyle.DEFAULT
                2 -> Render2D.TextStyle.BLACK_OUTLINE
                else -> Render2D.TextStyle.DROP_SHADOW
            }
            renderText(context, manaText, x, y, width, scale, manaTextStyle)
        }
    }

    fun riftTimeBarEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, timePerc: Double = 0.8) {
        renderBar(context, x, y, width, height, scale, timePerc, Color.GREEN)

        if (showRiftTimeText || previewMode) {
            val timeValue = if (previewMode) {
                "48m 32s"
            } else {
                val minutes = PlayerStats.riftTimeSeconds / 60
                val seconds = PlayerStats.riftTimeSeconds % 60
                "${minutes}m ${seconds}s"
            }

            val timeText = "${riftTimeTextColor.code}$timeValue"
            renderText(context, timeText, x, y, width, scale)
        }
    }

    fun drillFuelBarEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, fuelPerc: Double = 0.7) {
        renderBar(context, x, y, width, height, scale, fuelPerc, Color(0, 128, 0))

        if (showDrillFuelText || previewMode) {
            val currentFuel = if (previewMode) {
                (1000 * fuelPerc).toInt()
            } else {
                PlayerStats.drillFuel
            }

            val fuelText = if (showMaxDrillFuel) {
                val maxValue = if (previewMode) 1000 else PlayerStats.maxDrillFuel
                "${drillFuelTextColor.code}$currentFuel§8/${maxDrillFuelTextColor.code}$maxValue"
            } else "${drillFuelTextColor.code}$currentFuel"

            renderText(context, fuelText, x, y, width, scale)
        }
    }
}