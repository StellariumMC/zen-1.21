package xyz.meowing.zen.features.general

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.ui.constraint.ChildHeightConstraint
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.config.ConfigManager.configUI
import java.awt.Color
import java.text.DecimalFormat

enum class DamageType(val displayName: String, val symbol: String, val chatColor: String, val guiColor: Color) {
    CRIT("Crit Hits", "✧", "§b§l", Color(85, 170, 255, 255)),
    OVERLOAD("Overload Hits", "✯", "§d§l", Color(255, 85, 255, 255)),
    FIRE("Fire Hits", "🔥", "§6§l", Color(255, 170, 0, 255)),
    NORMAL("Non-Crit Hits", "⚔", "§f", Color(200, 200, 200, 255))
}

data class DamageEntry(
    val damage: Int,
    val type: DamageType,
    val timestamp: Long = System.currentTimeMillis()
)

data class DamageStats(
    val entries: MutableList<DamageEntry> = mutableListOf(),
    var enabledTypes: MutableSet<DamageType> = mutableSetOf(DamageType.CRIT)
)

@Module
object DamageTracker : Feature("damagetracker", true) {
    private val selectedTypes by ConfigDelegate<Set<Int>>("damagetrackertype")
    private val damagetrackersend by ConfigDelegate<Boolean>("damagetrackersend")

    val stats = DamageStats()
    private val formatter = DecimalFormat("#,###")
    private var lastHitEntity: Entity? = null
    private var lastHitTime = 0L

    override fun addConfig() {
        ConfigManager
            .addFeature("Damage Tracker", "Track damage dealt to mobs", "General", ConfigElement(
                "damagetracker",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Damage Tracker Info", "", "", ConfigElement(
                "",
                ElementType.TextParagraph("This does not track the damage done by arrows shot using duplex or the extra arrows from Terminator")
            ))
            .addFeatureOption("Hit detection types", "Hit detection types", "Options", ConfigElement(
                "damagetrackertype",
                ElementType.MultiCheckbox(
                    options = DamageType.entries.map { it.displayName },
                    default = setOf(0)
                )
            ))
            .addFeatureOption("Show damage in chat", "Show damage in chat", "Options", ConfigElement(
                "damagetrackersend",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Damage Stats GUI", "Damage Stats GUI", "GUI", ConfigElement(
                "damagetrackergui",
                ElementType.Button("Open Stats") {
                    TickUtils.schedule(2) {
                        client.setScreen(DamageTrackerGui())
                    }
                }
            ))
            .addFeatureOption("Damage Tracker GUI Info", "", "GUI", ConfigElement(
                "",
                ElementType.TextParagraph("Use the command §c/damagetracker §rto open the Stats GUI. §7§oAlias: /zendt, /dmg")
            ))
    }

    override fun initialize() {
        updateEnabledTypes()

        configUI.registerListener("damagetrackertype") {
            updateEnabledTypes()
        }

        register<EntityEvent.Attack> { event ->
            val player = player ?: return@register
            val playerName = Utils.currentPlayerName

            if (event.player.name.string != playerName) return@register

            lastHitEntity = event.target
            lastHitTime = System.currentTimeMillis()
        }

        register<EntityEvent.ArrowHit> { event ->
            val player = player ?: return@register

            val playerName = Utils.currentPlayerName
            if (event.shooterName != playerName) return@register

            lastHitEntity = event.hitEntity
        }

        register<SkyblockEvent.DamageSplash> { event ->
            val lastHit = lastHitEntity ?: return@register

            val hitEntityPos = Vec3d(lastHit.x, lastHit.y + lastHit.height / 2, lastHit.z)
            val distance = event.entityPos.distanceTo(hitEntityPos)

            if (distance > 3.0) return@register

            val type = detectDamageType(event.originalName, event.originalName.removeFormatting())
            if (!stats.enabledTypes.contains(type)) return@register

            stats.entries.add(DamageEntry(event.damage, type))
            if (stats.entries.size > 1000) stats.entries.removeAt(0)

            if (damagetrackersend) {
                val formattedDamage = formatter.format(event.damage)
                val message = "${type.chatColor}${type.symbol} §r${type.chatColor}$formattedDamage §8[${type.displayName}]"
                KnitChat.fakeMessage("$prefix $message")
            }
        }
    }

    private fun updateEnabledTypes() {
        stats.enabledTypes.clear()
        selectedTypes.forEach { index ->
            if (index < DamageType.entries.size) stats.enabledTypes.add(DamageType.entries[index])
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

    fun getStats(type: DamageType? = null): Triple<Int, Int, Double> {
        val filteredEntries = if (type != null) {
            stats.entries.filter { it.type == type }
        } else {
            stats.entries
        }

        if (filteredEntries.isEmpty()) return Triple(0, 0, 0.0)

        val total = filteredEntries.sumOf { it.damage }
        val max = filteredEntries.maxOf { it.damage }
        val avg = total.toDouble() / filteredEntries.size

        return Triple(total, max, avg)
    }

    fun clearStats() {
        stats.entries.clear()
    }
}

@Command
object DamageTrackerCommand : Commodore("damagetracker", "dt", "dmg") {
    init {
        runs {
            TickUtils.schedule(2) {
                client.setScreen(DamageTrackerGui())
            }
        }
    }
}

class DamageTrackerGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(12, 16, 20, 255)
        val accent = Color(100, 245, 255, 255)
        val accent2 = Color(80, 200, 220, 255)
        val danger = Color(115, 41, 41, 255)
        val divider = Color(30, 35, 40, 255)
    }

    private lateinit var scrollComponent: ScrollComponent
    private lateinit var statsContainer: UIContainer
    private val formatter = DecimalFormat("#,###.0")

    init {
        buildGui()
        updateStats()
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 80.percent()
            height = 85.percent()
        }.setColor(theme.accent2) childOf window

        val main = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.bg) childOf border

        createHeader(main)
        createContent(main)
        createFooter(main)
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 50.pixels()
        } childOf parent

        UIText("§lDamage Tracker").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 2.0.pixels()
        }.setColor(theme.accent) childOf header

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf header
    }

    private fun createContent(parent: UIComponent) {
        val contentPanel = UIContainer().constrain {
            x = 8.pixels()
            y = 58.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 106.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        statsContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(8f)
        } childOf scrollComponent
    }

    private fun createFooter(parent: UIComponent) {
        val footer = UIContainer().constrain {
            x = 8.pixels()
            y = 100.percent() - 40.pixels()
            width = 100.percent() - 16.pixels()
            height = 40.pixels()
        } childOf parent

        createBlock(0f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf footer

        val clearButton = createBlock(3f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 80.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf footer

        clearButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            DamageTracker.clearStats()
            updateStats()
        }

        UIText("Clear Stats").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(Color.WHITE) childOf clearButton
    }

    private fun updateStats() {
        statsContainer.clearChildren()

        createOverallStats()
        createTypeStats()
        createRecentHits()
    }

    private fun createOverallStats() {
        val (total, max, avg) = DamageTracker.getStats()
        val count = DamageTracker.stats.entries.size

        if (count == 0) {
            createStatRow("No damage recorded yet", "", theme.accent2)
            return
        }

        createSectionHeader("Overall Statistics")
        createStatRow("Total Hits", formatter.format(count.toDouble()).replace(".0", ""), theme.accent)
        createStatRow("Total Damage", formatter.format(total.toDouble()).replace(".0", ""), theme.accent)
        createStatRow("Average Damage", formatter.format(avg), theme.accent)
        createStatRow("Highest Hit", formatter.format(max.toDouble()).replace(".0", ""), theme.accent)
    }

    private fun createTypeStats() {
        if (DamageTracker.stats.entries.isEmpty()) return

        createSectionHeader("Damage by Type")

        val typesWithData = DamageType.entries.filter { type ->
            DamageTracker.stats.entries.any { it.type == type }
        }

        typesWithData.chunked(2).forEach { rowTypes ->
            val rowContainer = UIContainer().constrain {
                x = 0.percent()
                y = CramSiblingConstraint(4f)
                width = 100.percent()
                height = 60.pixels()
            } childOf statsContainer

            rowTypes.forEachIndexed { index, type ->
                val (total, max, avg) = DamageTracker.getStats(type)
                val count = DamageTracker.stats.entries.filter { it.type == type }.size

                val column = createBlock(3f).constrain {
                    x = if (index == 0) 0.percent() else 50.percent() + 2.pixels()
                    y = 0.percent()
                    width = if (rowTypes.size == 1) 100.percent() else 50.percent() - 2.pixels()
                    height = 100.percent()
                }.setColor(theme.element) childOf rowContainer

                UIText("${type.displayName} ${type.symbol}").constrain {
                    x = 12.pixels()
                    y = 8.pixels()
                    textScale = 1.2.pixels()
                }.setColor(type.guiColor) childOf column

                UIText("Count: ${formatter.format(count.toDouble()).replace(".0", "")}").constrain {
                    x = 12.pixels()
                    y = 24.pixels()
                    textScale = 0.9.pixels()
                }.setColor(theme.accent2) childOf column

                UIText("Avg: ${formatter.format(avg)}").constrain {
                    x = 8.pixels(true)
                    y = 8.pixels()
                    textScale = 0.9.pixels()
                }.setColor(theme.accent2) childOf column

                UIText("Max: ${formatter.format(max.toDouble()).replace(".0", "")}").constrain {
                    x = 8.pixels(true)
                    y = 24.pixels()
                    textScale = 0.9.pixels()
                }.setColor(theme.accent2) childOf column

                UIText("Total: ${formatter.format(total.toDouble()).replace(".0", "")}").constrain {
                    x = 8.pixels(true)
                    y = 40.pixels()
                    textScale = 0.9.pixels()
                }.setColor(theme.accent2) childOf column
            }
        }
    }

    private fun createRecentHits() {
        val recentEntries = DamageTracker.stats.entries.takeLast(10).reversed()
        if (recentEntries.isEmpty()) return

        createSectionHeader("Recent Hits (Last 10)")

        recentEntries.forEach { entry ->
            createStatRow(
                "${entry.type.displayName} ${entry.type.symbol}",
                formatter.format(entry.damage.toDouble()).replace(".0", ""),
                entry.type.guiColor
            )
        }
    }

    private fun createSectionHeader(text: String) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(8f)
            width = 100.percent()
            height = 30.pixels()
        } childOf statsContainer

        UIText(text).constrain {
            x = 0.percent()
            y = CenterConstraint()
            textScale = 1.4.pixels()
        }.setColor(theme.accent) childOf header

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.divider) childOf header
    }

    private fun createStatRow(label: String, value: String, color: Color) {
        val row = createBlock(2f).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(2f)
            width = 100.percent()
            height = 24.pixels()
        }.setColor(theme.element) childOf statsContainer

        UIText(label).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(color) childOf row

        UIText(value).constrain {
            x = 8.pixels(alignOpposite = true)
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(theme.accent2) childOf row
    }
}