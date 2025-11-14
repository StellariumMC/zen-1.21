package xyz.meowing.zen.features.general.damageTracker
// TODO: Refactor sometime - too big of a pain and like no one uses it.
//import gg.essential.elementa.ElementaVersion
//import gg.essential.elementa.UIComponent
//import gg.essential.elementa.WindowScreen
//import gg.essential.elementa.components.*
//import gg.essential.elementa.constraints.*
//import gg.essential.elementa.constraints.animation.Animations
//import gg.essential.elementa.dsl.*
//import xyz.meowing.zen.ui.constraint.ChildHeightConstraint
//import java.awt.Color
//import java.text.DecimalFormat
//
//class DamageTrackerGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
//    private val theme = object {
//        val bg = Color(8, 12, 16, 255)
//        val element = Color(12, 16, 20, 255)
//        val accent = Color(100, 245, 255, 255)
//        val accent2 = Color(80, 200, 220, 255)
//        val danger = Color(115, 41, 41, 255)
//        val divider = Color(30, 35, 40, 255)
//    }
//
//    private lateinit var scrollComponent: ScrollComponent
//    private lateinit var statsContainer: UIContainer
//    private val formatter = DecimalFormat("#,###.0")
//
//    init {
//        buildGui()
//        updateStats()
//    }
//
//    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)
//
//    private fun buildGui() {
//        val border = createBlock(4f).constrain {
//            x = CenterConstraint()
//            y = CenterConstraint()
//            width = 80.percent()
//            height = 85.percent()
//        }.setColor(theme.accent2) childOf window
//
//        val main = createBlock(4f).constrain {
//            x = CenterConstraint()
//            y = CenterConstraint()
//            width = 100.percent() - 2.pixels()
//            height = 100.percent() - 2.pixels()
//        }.setColor(theme.bg) childOf border
//
//        createHeader(main)
//        createContent(main)
//        createFooter(main)
//    }
//
//    private fun createHeader(parent: UIComponent) {
//        val header = UIContainer().constrain {
//            x = 0.percent()
//            y = 0.percent()
//            width = 100.percent()
//            height = 50.pixels()
//        } childOf parent
//
//        UIText("§lDamage Tracker").constrain {
//            x = CenterConstraint()
//            y = CenterConstraint()
//            textScale = 2.0.pixels()
//        }.setColor(theme.accent) childOf header
//
//        createBlock(0f).constrain {
//            x = 0.percent()
//            y = 100.percent() - 1.pixels()
//            width = 100.percent()
//            height = 1.pixels()
//        }.setColor(theme.accent2) childOf header
//    }
//
//    private fun createContent(parent: UIComponent) {
//        val contentPanel = UIContainer().constrain {
//            x = 8.pixels()
//            y = 58.pixels()
//            width = 100.percent() - 16.pixels()
//            height = 100.percent() - 106.pixels()
//        } childOf parent
//
//        scrollComponent = ScrollComponent().constrain {
//            x = 4.pixels()
//            y = 4.pixels()
//            width = 100.percent() - 8.pixels()
//            height = 100.percent() - 8.pixels()
//        } childOf contentPanel
//
//        statsContainer = UIContainer().constrain {
//            width = 100.percent()
//            height = ChildHeightConstraint(8f)
//        } childOf scrollComponent
//    }
//
//    private fun createFooter(parent: UIComponent) {
//        val footer = UIContainer().constrain {
//            x = 8.pixels()
//            y = 100.percent() - 40.pixels()
//            width = 100.percent() - 16.pixels()
//            height = 40.pixels()
//        } childOf parent
//
//        createBlock(0f).constrain {
//            x = 0.percent()
//            y = 0.percent()
//            width = 100.percent()
//            height = 1.pixels()
//        }.setColor(theme.accent2) childOf footer
//
//        val clearButton = createBlock(3f).constrain {
//            x = CenterConstraint()
//            y = CenterConstraint()
//            width = 80.pixels()
//            height = 24.pixels()
//        }.setColor(theme.element) childOf footer
//
//        clearButton.onMouseEnter {
//            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
//        }.onMouseLeave {
//            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
//        }.onMouseClick {
//            DamageTracker.clearStats()
//            updateStats()
//        }
//
//        UIText("Clear Stats").constrain {
//            x = CenterConstraint()
//            y = CenterConstraint()
//            textScale = 0.8.pixels()
//        }.setColor(Color.WHITE) childOf clearButton
//    }
//
//    private fun updateStats() {
//        statsContainer.clearChildren()
//
//        createOverallStats()
//        createTypeStats()
//        createRecentHits()
//    }
//
//    private fun createOverallStats() {
//        val (total, max, avg) = DamageTracker.getStats()
//        val count = DamageTracker.stats.entries.size
//
//        if (count == 0) {
//            createStatRow("No damage recorded yet", "", theme.accent2)
//            return
//        }
//
//        createSectionHeader("Overall Statistics")
//        createStatRow("Total Hits", formatter.format(count.toDouble()).replace(".0", ""), theme.accent)
//        createStatRow("Total Damage", formatter.format(total.toDouble()).replace(".0", ""), theme.accent)
//        createStatRow("Average Damage", formatter.format(avg), theme.accent)
//        createStatRow("Highest Hit", formatter.format(max.toDouble()).replace(".0", ""), theme.accent)
//    }
//
//    private fun createTypeStats() {
//        if (DamageTracker.stats.entries.isEmpty()) return
//
//        createSectionHeader("Damage by Type")
//
//        val typesWithData = DamageType.entries.filter { type ->
//            DamageTracker.stats.entries.any { it.type == type }
//        }
//
//        typesWithData.chunked(2).forEach { rowTypes ->
//            val rowContainer = UIContainer().constrain {
//                x = 0.percent()
//                y = CramSiblingConstraint(4f)
//                width = 100.percent()
//                height = 60.pixels()
//            } childOf statsContainer
//
//            rowTypes.forEachIndexed { index, type ->
//                val (total, max, avg) = DamageTracker.getStats(type)
//                val count = DamageTracker.stats.entries.filter { it.type == type }.size
//
//                val column = createBlock(3f).constrain {
//                    x = if (index == 0) 0.percent() else 50.percent() + 2.pixels()
//                    y = 0.percent()
//                    width = if (rowTypes.size == 1) 100.percent() else 50.percent() - 2.pixels()
//                    height = 100.percent()
//                }.setColor(theme.element) childOf rowContainer
//
//                UIText("${type.displayName} ${type.symbol}").constrain {
//                    x = 12.pixels()
//                    y = 8.pixels()
//                    textScale = 1.2.pixels()
//                }.setColor(type.guiColor) childOf column
//
//                UIText("Count: ${formatter.format(count.toDouble()).replace(".0", "")}").constrain {
//                    x = 12.pixels()
//                    y = 24.pixels()
//                    textScale = 0.9.pixels()
//                }.setColor(theme.accent2) childOf column
//
//                UIText("Avg: ${formatter.format(avg)}").constrain {
//                    x = 8.pixels(true)
//                    y = 8.pixels()
//                    textScale = 0.9.pixels()
//                }.setColor(theme.accent2) childOf column
//
//                UIText("Max: ${formatter.format(max.toDouble()).replace(".0", "")}").constrain {
//                    x = 8.pixels(true)
//                    y = 24.pixels()
//                    textScale = 0.9.pixels()
//                }.setColor(theme.accent2) childOf column
//
//                UIText("Total: ${formatter.format(total.toDouble()).replace(".0", "")}").constrain {
//                    x = 8.pixels(true)
//                    y = 40.pixels()
//                    textScale = 0.9.pixels()
//                }.setColor(theme.accent2) childOf column
//            }
//        }
//    }
//
//    private fun createRecentHits() {
//        val recentEntries = DamageTracker.stats.entries.takeLast(10).reversed()
//        if (recentEntries.isEmpty()) return
//
//        createSectionHeader("Recent Hits (Last 10)")
//
//        recentEntries.forEach { entry ->
//            createStatRow(
//                "${entry.type.displayName} ${entry.type.symbol}",
//                formatter.format(entry.damage.toDouble()).replace(".0", ""),
//                entry.type.guiColor
//            )
//        }
//    }
//
//    private fun createSectionHeader(text: String) {
//        val header = UIContainer().constrain {
//            x = 0.percent()
//            y = CramSiblingConstraint(8f)
//            width = 100.percent()
//            height = 30.pixels()
//        } childOf statsContainer
//
//        UIText(text).constrain {
//            x = 0.percent()
//            y = CenterConstraint()
//            textScale = 1.4.pixels()
//        }.setColor(theme.accent) childOf header
//
//        createBlock(0f).constrain {
//            x = 0.percent()
//            y = 100.percent() - 1.pixels()
//            width = 100.percent()
//            height = 1.pixels()
//        }.setColor(theme.divider) childOf header
//    }
//
//    private fun createStatRow(label: String, value: String, color: Color) {
//        val row = createBlock(2f).constrain {
//            x = 0.percent()
//            y = CramSiblingConstraint(2f)
//            width = 100.percent()
//            height = 24.pixels()
//        }.setColor(theme.element) childOf statsContainer
//
//        UIText(label).constrain {
//            x = 8.pixels()
//            y = CenterConstraint()
//            textScale = 0.9.pixels()
//        }.setColor(color) childOf row
//
//        UIText(value).constrain {
//            x = 8.pixels(alignOpposite = true)
//            y = CenterConstraint()
//            textScale = 0.9.pixels()
//        }.setColor(theme.accent2) childOf row
//    }
//}