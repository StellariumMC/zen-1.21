package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.api.slayer.SlayerTracker
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.utils.NumberUtils.formatWithCommas
import xyz.meowing.knit.api.utils.NumberUtils.toDuration
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Module
object SlayerStats : Feature(
    "slayerStats",
    true
) {
    private const val NAME = "Slayer Stats"
    private val slayerStatsLines by ConfigDelegate<Set<Int>>("slayerStats.lines")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Slayer stats",
                "Shows slayer statistics such as total bosses killed, bosses per hour, and average kill time. §c/slayerstats reset §rto reset stats.",
                "Slayers",
                ConfigElement(
                    "slayerStats",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Lines to show",
                ConfigElement(
                    "slayerStats.lines",
                    ElementType.MultiCheckbox(
                        options = listOf(
                            "Show Bosses Killed",
                            "Show Bosses/hr",
                            "Show Average kill time",
                            "Show Average spawn time",
                            "Show Total Session time",
                            "Show XP/hr"
                        ),
                        default = setOf(0, 1, 4, 5)
                    )
                )
            )
    }


    override fun initialize() {
        HUDManager.register(
            NAME,
            "$prefix §f§lSlayer Stats: \n§7> §bBosses Killed§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s",
            "slayerStats"
        )

        register<GuiEvent.Render.HUD> {
            render(it.context)
        }
    }

    private fun getBPH(): Int {
        if (SlayerTracker.sessionBossKills == 0) return 0

        val avgTotal = ((SlayerTracker.totalKillTime + SlayerTracker.totalSpawnTime).millis / SlayerTracker.sessionBossKills / 1000.0) // Avg Total Time in seconds
        val bph = (3600.0 / avgTotal).toInt()
        return bph
    }

    fun reset() {
        SlayerTracker.reset()
        KnitChat.fakeMessage("$prefix §fSlayer stats reset!")
    }

    private fun render(context: GuiGraphics) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        val lines = getLines()

        if (lines.isNotEmpty()) {
            var currentY = y
            for (line in lines) {
                Render2D.renderString(context, line, x, currentY, scale)
                currentY += client.font.lineHeight + 2
            }
        }
    }

    private fun getLines(): List<String> {
        if (SlayerTracker.mobLastKilledAt.since.inWholeMinutes > 5 || SlayerTracker.mobLastKilledAt.isZero) {
            return emptyList()
        }

        val list = mutableListOf("$prefix §f§lSlayer Stats: ")

        if (slayerStatsLines.contains(4)) {
            if (SlayerTracker.sessionStart.isZero) {
                list.add(" §7> §bSession time§f: §c-")
            } else {
                val pauseMark = SlayerTracker.pauseStart
                val totalTime = TimeUtils.now - SlayerTracker.sessionStart - (pauseMark?.since ?: Duration.ZERO) - SlayerTracker.totalSessionPaused.milliseconds
                val timeString = totalTime.millis.toDuration(false)
                list.add(" §7> §bSession time§f: §c$timeString" + if (SlayerTracker.isPaused) " §7(Paused)" else "")
            }
        }

        slayerStatsLines.sorted().forEach { line ->
            when (line) {
                0 -> list.add(" §7> §bBosses Killed§f: §c${SlayerTracker.sessionBossKills}")
                1 -> list.add(" §7> §bBosses/hr§f: §c${if (SlayerTracker.sessionBossKills == 0) "-" else getBPH()}")
                2 -> {
                    val avgKill = if (SlayerTracker.sessionBossKills == 0) "-"
                    else (SlayerTracker.totalKillTime.millis / SlayerTracker.sessionBossKills / 1000.0).format(1) + "s"
                    list.add(" §7> §bAvg. kill§f: §c$avgKill")
                }
                3 -> {
                    val avgSpawn = if (SlayerTracker.sessionBossKills == 0) "-"
                    else (SlayerTracker.totalSpawnTime.millis / SlayerTracker.sessionBossKills / 1000.0).format(1) + "s"
                    list.add(" §7> §bAvg. spawn§f: §c$avgSpawn")
                }
                5 -> {
                    val xpPH = getBPH() * SlayerTracker.xpPerKill
                    list.add(" §7> §bXP/hr§f: §c${xpPH.formatWithCommas()} XP")
                }
            }
        }

        return list
    }
    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
}

@Command
object SlayerStatsCommand : Commodore("slayerstats", "zenslayerstats") {
    init {
        executable {
            runs { action: String ->
                if (action == "reset") {
                    SlayerStats.reset()
                } else {
                    KnitChat.fakeMessage("$prefix §fPlease use §c/slayerstats reset")
                }
            }
        }

        runs {
            KnitChat.fakeMessage("$prefix §fPlease use §c/slayerstats reset")
        }
    }
}