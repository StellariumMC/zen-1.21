package xyz.meowing.zen.features.hud

// TODO: Refactor sometime.

//
//import xyz.meowing.zen.api.skyblock.PlayerStats
//import xyz.meowing.zen.config.ConfigDelegate
//import xyz.meowing.zen.config.ui.elements.MCColorCode
//import xyz.meowing.zen.config.ui.elements.base.ElementType
//import xyz.meowing.zen.events.configRegister
//import xyz.meowing.zen.features.Feature
//import xyz.meowing.zen.hud.HUDManager
//import xyz.meowing.zen.utils.Render2D
//import xyz.meowing.zen.utils.Render2D.width
//import net.minecraft.client.gui.GuiGraphics
//import net.minecraft.network.chat.Component
//import xyz.meowing.knit.api.KnitClient
//import xyz.meowing.knit.api.KnitPlayer.player
//import xyz.meowing.knit.api.text.core.FormattingCodes
//import xyz.meowing.vexel.utils.render.NVGRenderer
//import xyz.meowing.zen.annotations.Module
//import xyz.meowing.zen.events.core.ChatEvent
//import xyz.meowing.zen.events.core.GuiEvent
//import xyz.meowing.zen.managers.config.ConfigElement
//import xyz.meowing.zen.managers.config.ConfigManager
//import xyz.meowing.zen.utils.Utils.removeFormatting
//import java.awt.Color
//
//@Module
//object StatsDisplay : Feature(
//    "statsdisplay",
//    true
//) {
//    private const val HEALTH_BAR_NAME = "Health Bar"
//    private const val MANA_BAR_NAME = "Mana Bar"
//    private const val OVERFLOW_MANA_BAR_NAME = "Overflow Mana"
//    private const val RIFT_TIME_BAR_NAME = "Rift Time Bar"
//    private const val DRILL_FUEL_BAR_NAME = "Drill Fuel Bar"
//    private const val PROCESSED_MARKER = "§z§e§b"
//
//    private data class ColoredSegment(val text: String, val color: Int)
//
//    private val coloredTextCache = object : LinkedHashMap<String, List<ColoredSegment>>(100, 0.75f, true) {
//        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<ColoredSegment>>?): Boolean {
//            return size > 100
//        }
//    }
//
//    private val hiddenStats by ConfigDelegate<Set<Int>>("statsDisplay.hiddenStats")
//    private val hideVanillaHp by ConfigDelegate<Boolean>("statsDisplay.hideVanillaHp")
//    private val hideVanillaArmor by ConfigDelegate<Boolean>("statsDisplay.hideVanillaArmor")
//    private val hideExpBar by ConfigDelegate<Boolean>("statsDisplay.hideExpBar")
//
//    @JvmStatic fun shouldHideVanillaHealth(): Boolean = isEnabled() && hideVanillaHp
//    @JvmStatic fun shouldHideVanillaArmor(): Boolean = isEnabled() && hideVanillaArmor
//    @JvmStatic fun shouldHideExpBar(): Boolean = isEnabled() && hideExpBar
//
//    private val showHealthBar by ConfigDelegate<Boolean>("statsDisplay.showHealthBar")
//    private val healthBarFillColor by ConfigDelegate<Color>("statsDisplay.healthBarFillColor")
//    private val healthBarExtraColor by ConfigDelegate<Color>("statsDisplay.healthBarExtraColor")
//    private val healthTextStyle by ConfigDelegate<Int>("statsDisplay.healthTextStyle")
//    private val showHealthText by ConfigDelegate<Boolean>("statsDisplay.showHealthText")
//    private val showMaxHealth by ConfigDelegate<Boolean>("statsDisplay.showMaxHealth")
//    private val healthTextColor by ConfigDelegate<MCColorCode>("statsDisplay.healthTextColor")
//    private val maxHealthTextColor by ConfigDelegate<MCColorCode>("statsDisplay.maxHealthTextColor")
//
//    private val showManaBar by ConfigDelegate<Boolean>("statsDisplay.showManaBar")
//    private val manaBarFillColor by ConfigDelegate<Color>("statsDisplay.manaBarFillColor")
//    private val manaTextStyle by ConfigDelegate<Int>("statsDisplay.manaTextStyle")
//    private val showManaText by ConfigDelegate<Boolean>("statsDisplay.showManaText")
//    private val showMaxMana by ConfigDelegate<Boolean>("statsDisplay.showMaxMana")
//    private val manaTextColor by ConfigDelegate<MCColorCode>("statsDisplay.manaTextColor")
//    private val maxManaTextColor by ConfigDelegate<MCColorCode>("statsDisplay.maxManaTextColor")
//
//    private val showOverflowManaText by ConfigDelegate<Boolean>("statsDisplay.showOverflowManaText")
//    private val overflowManaTextColor by ConfigDelegate<MCColorCode>("statsDisplay.overflowManaTextColor")
//
//    private val showRiftTimeText by ConfigDelegate<Boolean>("statsDisplay.showRiftTimeText")
//    private val riftTimeTextColor by ConfigDelegate<MCColorCode>("statsDisplay.riftTimeTextColor")
//
//    private val showDrillFuelText by ConfigDelegate<Boolean>("statsDisplay.showDrillFuelText")
//    private val showMaxDrillFuel by ConfigDelegate<Boolean>("statsDisplay.showMaxDrillFuel")
//    private val drillFuelTextColor by ConfigDelegate<MCColorCode>("statsDisplay.drillFuelTextColor")
//    private val maxDrillFuelTextColor by ConfigDelegate<MCColorCode>("statsDisplay.maxDrillFuelTextColor")
//
//    private enum class StatType(val displayName: String, val regex: Regex) {
//        HEALTH("Health", """(§.)(?<currentHealth>[\d,]+)/(?<maxHealth>[\d,]+)❤""".toRegex()),
//        MANA("Mana", """§b(?<currentMana>[\d,]+)/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()),
//        OVERFLOW_MANA("Overflow Mana", """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()),
//        DEFENSE("Defense", """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()),
//        RIFT_TIME("Rift Time", """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()),
//        DRILL_FUEL("Drill Fuel", """§2(?<currentFuel>[\d,]+)/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()),
//    }
//
//    override fun addConfig() {
//        ConfigManager
//            .addFeature(
//                "Stats display",
//                "Display stats on HUD",
//                "HUD",
//                ConfigElement(
//                    "statsDisplay",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Clean action bar",
//                ConfigElement(
//                    "statsDisplay.cleanActionBar",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Hide vanilla HP and saturation",
//                ConfigElement(
//                    "statsDisplay.hideVanillaHp",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Hide armor icon",
//                ConfigElement(
//                    "statsDisplay.hideVanillaArmor",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Hide experience bar",
//                ConfigElement(
//                    "statsDisplay.hideExpBar",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Hide stats",
//                ConfigElement(
//                    "statsDisplay.hiddenStats",
//                    ElementType.MultiCheckbox(
//                        options = StatType.entries.map { it.displayName },
//                        default = emptySet()
//                    )
//                )
//            )
//            .addFeatureOption(
//                "Show health bar",
//                ConfigElement(
//                    "statsDisplay.showHealthBar",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Health bar fill color",
//                ConfigElement(
//                    "statsDisplay.healthBarFillColor",
//                    ElementType.ColorPicker(MCColorCode.RED.color)
//                )
//            )
//            .addFeatureOption(
//                "Health bar absorption fill color",
//                ConfigElement(
//                    "statsDisplay.healthBarExtraColor",
//                    ElementType.ColorPicker(MCColorCode.YELLOW.color)
//                )
//            )
//            .addFeatureOption(
//                "Show health numbers",
//                ConfigElement(
//                    "statsDisplay.showHealthText",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Health text style",
//                ConfigElement(
//                    "statsDisplay.healthTextStyle",
//                    ElementType.Dropdown(listOf("Shadow", "Default", "Outline"), 0)
//                )
//            )
//            .addFeatureOption(
//                "Show max health",
//                ConfigElement(
//                    "statsDisplay.showMaxHealth",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Health text color",
//                ConfigElement(
//                    "statsDisplay.healthTextColor",
//                    ElementType.MCColorPicker(MCColorCode.RED)
//                )
//            )
//            .addFeatureOption(
//                "Max health text color",
//                ConfigElement(
//                    "statsDisplay.maxHealthTextColor",
//                    ElementType.MCColorPicker(MCColorCode.RED)
//                )
//            )
//            .addFeatureOption(
//                "Show mana bar",
//                ConfigElement(
//                    "statsDisplay.showManaBar",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Mana bar fill color",
//                ConfigElement(
//                    "statsDisplay.manaBarFillColor",
//                    ElementType.ColorPicker(MCColorCode.BLUE.color)
//                )
//            )
//            .addFeatureOption(
//                "Show mana numbers",
//                ConfigElement(
//                    "statsDisplay.showManaText",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Mana text style",
//                ConfigElement(
//                    "statsDisplay.manaTextStyle",
//                    ElementType.Dropdown(listOf("Shadow", "Default", "Outline"), 0)
//                )
//            )
//            .addFeatureOption(
//                "Show max mana",
//                ConfigElement(
//                    "statsDisplay.showMaxMana",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Mana text color",
//                ConfigElement(
//                    "statsDisplay.manaTextColor",
//                    ElementType.MCColorPicker(MCColorCode.BLUE)
//                )
//            )
//            .addFeatureOption(
//                "Max mana text color",
//                ConfigElement(
//                    "statsDisplay.maxManaTextColor",
//                    ElementType.MCColorPicker(MCColorCode.BLUE)
//                )
//            )
//            .addFeatureOption(
//                "Show overflow mana",
//                ConfigElement(
//                    "statsDisplay.showOverflowManaText",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Overflow mana text color",
//                ConfigElement(
//                    "statsDisplay.overflowManaTextColor",
//                    ElementType.MCColorPicker(MCColorCode.DARK_AQUA)
//                )
//            )
//            .addFeatureOption(
//                "Show rift time text",
//                ConfigElement(
//                    "statsDisplay.showRiftTimeText",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Rift time text color",
//                ConfigElement(
//                    "statsDisplay.riftTimeTextColor",
//                    ElementType.MCColorPicker(MCColorCode.GREEN)
//                )
//            )
//            .addFeatureOption(
//                "Show drill fuel numbers",
//                ConfigElement(
//                    "statsDisplay.showDrillFuelText",
//                    ElementType.Switch(true)
//                )
//            )
//            .addFeatureOption(
//                "Show max drill fuel",
//                ConfigElement(
//                    "statsDisplay.showMaxDrillFuel",
//                    ElementType.Switch(false)
//                )
//            )
//            .addFeatureOption(
//                "Drill fuel text color",
//                ConfigElement(
//                    "statsDisplay.drillFuelTextColor",
//                    ElementType.MCColorPicker(MCColorCode.DARK_GREEN)
//                )
//            )
//            .addFeatureOption(
//                "Max drill fuel text color",
//                ConfigElement(
//                    "statsDisplay.maxDrillFuelTextColor",
//                    ElementType.MCColorPicker(MCColorCode.GREEN)
//                )
//            )
//    }
//
//    override fun initialize() {
//        HUDManager.register(OVERFLOW_MANA_BAR_NAME, "§3642ʬ")
//        HUDManager.registerCustom(HEALTH_BAR_NAME, 80, 10, this::healthBarEditorRender)
//        HUDManager.registerCustom(MANA_BAR_NAME, 80, 10, this::manaBarEditorRender)
//        HUDManager.registerCustom(RIFT_TIME_BAR_NAME, 80, 10, this::riftTimeBarEditorRender)
//        HUDManager.registerCustom(DRILL_FUEL_BAR_NAME, 80, 10, this::drillFuelBarEditorRender)
//
//        configRegister<ChatEvent.Receive>(listOf("statsDisplay", "statsDisplay.cleanActionBar"), priority = 1000, skyblockOnly = true) { event ->
//            if (!event.isActionBar) return@configRegister
//            val originalText = event.message.string
//
//            if (originalText.endsWith(PROCESSED_MARKER)) return@configRegister
//
//            val cleanedText = hiddenStats.fold(originalText) { text, index ->
//                StatType.entries.getOrNull(index)?.regex?.replace(text, "") ?: text
//            }.trim().replace("§r  ", " ")
//
//            if (cleanedText != originalText) {
//                event.cancel()
//                player?.displayClientMessage(Component.literal(cleanedText + PROCESSED_MARKER), true)
//            }
//        }
//
//        register<GuiEvent.Render.HUD> { event ->
//            val window = KnitClient.client.window
//            val guiScale = window.guiScale.toFloat()
//
//            //#if MC >= 1.21.7
//            //$$ event.context.matrices.pushMatrix()
//            //#else
//            event.context.pose().pushPose()
//            //#endif
//
//            NVGRenderer.beginFrame(window.screenWidth.toFloat(), window.screenHeight.toFloat())
//            NVGRenderer.push()
//            NVGRenderer.scale(guiScale, guiScale)
//            renderHealthBar(event.context)
//            renderManaBar(event.context)
//            renderOverflowMana(event.context)
//            renderRiftTimeBar(event.context)
//            renderDrillFuelBar(event.context)
//            NVGRenderer.pop()
//            NVGRenderer.endFrame()
//
//            //#if MC >= 1.21.7
//            //$$ event.context.matrices.popMatrix()
//            //#else
//            event.context.pose().popPose()
//            //#endif
//        }
//    }
//
//    private fun renderBar(context: GuiGraphics, x: Float, y: Float, width: Int, height: Int, scale: Float, primaryFill: Double, primaryColor: Color, secondaryFill: Double = 0.0, secondaryColor: Color? = null) {
//        val scaledWidth = width * scale
//        val scaledHeight = height * scale
//        val borderWidth = 1f * scale
//        val fillHeight = 8f * scale
//        val radius = 2f * scale
//
//        NVGRenderer.rect(x, y, scaledWidth, scaledHeight, Color.BLACK.rgb, radius)
//        NVGRenderer.rect(x + borderWidth, y + borderWidth, scaledWidth - 2 * borderWidth, fillHeight, Color.DARK_GRAY.rgb, radius * 0.75f)
//
//        val availableWidth = scaledWidth - 2 * borderWidth
//        val primaryWidth = (availableWidth * primaryFill).toFloat()
//        val secondaryWidth = (availableWidth * secondaryFill).toFloat()
//
//        if (primaryWidth > 0) {
//            NVGRenderer.rect(x + borderWidth, y + borderWidth, primaryWidth, fillHeight, primaryColor.rgb, radius * 0.75f)
//        }
//
//        if (secondaryFill > 0 && secondaryColor != null && secondaryWidth > 0) {
//            NVGRenderer.rect(x + borderWidth + primaryWidth, y + borderWidth, secondaryWidth, fillHeight, secondaryColor.rgb, radius * 0.75f)
//        }
//    }
//
//    private fun renderText(context: GuiGraphics, text: String, x: Float, y: Float, width: Int, scale: Float, style: Render2D.TextStyle = Render2D.TextStyle.DROP_SHADOW) {
//        val textWidth = NVGRenderer.textWidth(text.removeFormatting(), 8f * scale, NVGRenderer.defaultFont)
//        val scaledWidth = width * scale
//        val centerX = x + scaledWidth / 2f
//        val textX = centerX - textWidth / 2f
//        val textY = if (text.contains("ʬ")) y else y - (8f * scale)
//        var currentX = textX
//        parseColoredText(text).forEach { segment ->
//            NVGRenderer.textShadow(segment.text, currentX, textY, 8f * scale, segment.color, NVGRenderer.defaultFont)
//            currentX += NVGRenderer.textWidth(segment.text, 8f * scale, NVGRenderer.defaultFont)
//        }
//    }
//
//    private fun renderHealthBar(context: GuiGraphics) {
//        if (PlayerStats.maxHealth == 0) return
//        val max = PlayerStats.maxHealth
//        val absorption = PlayerStats.absorption
//        val health = PlayerStats.displayedHealth
//        val total = max + absorption
//        val healthFillPerc = health.toDouble() / total
//        val absorbFillPerc = absorption.toDouble() / total
//        val x = HUDManager.getX(HEALTH_BAR_NAME)
//        val y = HUDManager.getY(HEALTH_BAR_NAME)
//        val scale = HUDManager.getScale(HEALTH_BAR_NAME)
//
//        healthBarEditorRender(context, x, y, 80, 10, scale, 0f, false, healthFillPerc, absorbFillPerc)
//    }
//
//    private fun renderManaBar(context: GuiGraphics) {
//        if (PlayerStats.maxMana == 0) return
//        val max = PlayerStats.maxMana
//        val current = PlayerStats.displayedMana
//        val fillPerc = current.toDouble() / max
//        val x = HUDManager.getX(MANA_BAR_NAME)
//        val y = HUDManager.getY(MANA_BAR_NAME)
//        val scale = HUDManager.getScale(MANA_BAR_NAME)
//
//        manaBarEditorRender(context, x, y, 80, 10, scale, 0f, false, fillPerc)
//    }
//
//    private fun renderOverflowMana(context: GuiGraphics) {
//        if (!HUDManager.isEnabled(OVERFLOW_MANA_BAR_NAME)) return
//        val x = HUDManager.getX(OVERFLOW_MANA_BAR_NAME)
//        val y = HUDManager.getY(OVERFLOW_MANA_BAR_NAME)
//        val scale = HUDManager.getScale(OVERFLOW_MANA_BAR_NAME)
//
//        if (showOverflowManaText) {
//            val overflowMana = PlayerStats.overflowMana
//
//            if (overflowMana > 0) {
//                val overflowText = "${overflowManaTextColor.code}${overflowMana}ʬ"
//                renderText(context, overflowText, x, y, overflowText.width(), scale)
//            }
//        }
//    }
//
//    private fun renderRiftTimeBar(context: GuiGraphics) {
//        if (!HUDManager.isEnabled(RIFT_TIME_BAR_NAME) || PlayerStats.maxRiftTime == 0) return
//        val current = PlayerStats.riftTimeSeconds
//        val max = PlayerStats.maxRiftTime
//        val fillPerc = current.toDouble() / max
//        val x = HUDManager.getX(RIFT_TIME_BAR_NAME)
//        val y = HUDManager.getY(RIFT_TIME_BAR_NAME)
//        val scale = HUDManager.getScale(RIFT_TIME_BAR_NAME)
//
//        riftTimeBarEditorRender(context, x, y, 80, 10, scale, 0f, false, fillPerc)
//    }
//
//    private fun renderDrillFuelBar(context: GuiGraphics) {
//        if (!HUDManager.isEnabled(DRILL_FUEL_BAR_NAME) || PlayerStats.maxDrillFuel == 0) return
//        val max = PlayerStats.maxDrillFuel
//        val current = PlayerStats.drillFuel
//        val fillPerc = current.toDouble() / max
//        val x = HUDManager.getX(DRILL_FUEL_BAR_NAME)
//        val y = HUDManager.getY(DRILL_FUEL_BAR_NAME)
//        val scale = HUDManager.getScale(DRILL_FUEL_BAR_NAME)
//
//        drillFuelBarEditorRender(context, x, y, 80, 10, scale, 0f, false, fillPerc)
//    }
//
//    fun healthBarEditorRender(context: GuiGraphics, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, healthPerc: Double = 0.75, absorbPerc: Double = 0.25) {
//        if (showHealthBar) {
//            renderBar(context, x, y, width, height, scale, healthPerc, healthBarFillColor, absorbPerc, healthBarExtraColor)
//        }
//
//        if (showHealthText || previewMode) {
//            val currentHealth = if (previewMode) {
//                (1000 * (healthPerc + absorbPerc)).toInt()
//            } else {
//                PlayerStats.health + PlayerStats.absorption
//            }
//
//            val healthText = if (showMaxHealth) {
//                val maxValue = if (previewMode) 1000 else PlayerStats.maxHealth
//                "${healthTextColor.code}$currentHealth§8/${maxHealthTextColor.code}$maxValue"
//            } else {
//                "${healthTextColor.code}$currentHealth"
//            }
//
//            val healthTextStyle = when (healthTextStyle) {
//                0 -> Render2D.TextStyle.DROP_SHADOW
//                1 -> Render2D.TextStyle.DEFAULT
//                2 -> Render2D.TextStyle.BLACK_OUTLINE
//                else -> Render2D.TextStyle.DROP_SHADOW
//            }
//            renderText(context, healthText, x, y, width, scale, healthTextStyle)
//        }
//    }
//
//    fun manaBarEditorRender(context: GuiGraphics, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, manaPerc: Double = 0.6) {
//        if(showManaBar) {
//            renderBar(context, x, y, width, height, scale, manaPerc, manaBarFillColor)
//        }
//
//        if (showManaText || previewMode) {
//            val currentMana = if (previewMode) {
//                (1000 * manaPerc).toInt()
//            } else {
//                PlayerStats.mana
//            }
//
//            val manaText = if (showMaxMana) {
//                val maxValue = if (previewMode) 1000 else PlayerStats.maxMana
//                "${manaTextColor.code}$currentMana§8/${maxManaTextColor.code}$maxValue"
//            } else {
//                "${manaTextColor.code}$currentMana"
//            }
//
//            val manaTextStyle = when (manaTextStyle) {
//                0 -> Render2D.TextStyle.DROP_SHADOW
//                1 -> Render2D.TextStyle.DEFAULT
//                2 -> Render2D.TextStyle.BLACK_OUTLINE
//                else -> Render2D.TextStyle.DROP_SHADOW
//            }
//            renderText(context, manaText, x, y, width, scale, manaTextStyle)
//        }
//    }
//
//    fun riftTimeBarEditorRender(context: GuiGraphics, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, timePerc: Double = 0.8) {
//        renderBar(context, x, y, width, height, scale, timePerc, Color.GREEN)
//
//        if (showRiftTimeText || previewMode) {
//            val timeValue = if (previewMode) {
//                "48m 32s"
//            } else {
//                val minutes = PlayerStats.riftTimeSeconds / 60
//                val seconds = PlayerStats.riftTimeSeconds % 60
//                "${minutes}m ${seconds}s"
//            }
//
//            val timeText = "${riftTimeTextColor.code}$timeValue"
//            renderText(context, timeText, x, y, width, scale)
//        }
//    }
//
//    fun drillFuelBarEditorRender(context: GuiGraphics, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, fuelPerc: Double = 0.7) {
//        renderBar(context, x, y, width, height, scale, fuelPerc, Color(0, 128, 0))
//
//        if (showDrillFuelText || previewMode) {
//            val currentFuel = if (previewMode) {
//                (1000 * fuelPerc).toInt()
//            } else {
//                PlayerStats.drillFuel
//            }
//
//            val fuelText = if (showMaxDrillFuel) {
//                val maxValue = if (previewMode) 1000 else PlayerStats.maxDrillFuel
//                "${drillFuelTextColor.code}$currentFuel§8/${maxDrillFuelTextColor.code}$maxValue"
//            } else "${drillFuelTextColor.code}$currentFuel"
//
//            renderText(context, fuelText, x, y, width, scale)
//        }
//    }
//
//    private fun parseColoredText(text: String): List<ColoredSegment> {
//        return coloredTextCache.getOrPut(text) {
//            val segments = mutableListOf<ColoredSegment>()
//            var currentColor = -1
//            val currentText = StringBuilder()
//
//            var i = 0
//            while (i < text.length) {
//                if (i < text.length - 1 && text[i] == '§') {
//                    if (currentText.isNotEmpty()) {
//                        segments.add(ColoredSegment(currentText.toString(), currentColor))
//                        currentText.clear()
//                    }
//                    FormattingCodes.codeToColor(text[i + 1])?.let {
//                        currentColor = it or 0xFF000000.toInt()
//                    }
//                    i += 2
//                } else {
//                    currentText.append(text[i])
//                    i++
//                }
//            }
//
//            if (currentText.isNotEmpty()) {
//                segments.add(ColoredSegment(currentText.toString(), currentColor))
//            }
//
//            segments
//        }
//    }
//}