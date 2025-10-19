package xyz.meowing.zen.features.slayers

import com.google.gson.JsonObject
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.api.SlayerTracker.bossType
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.ui.ConfigMenuManager
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.utils.Utils.decodeRoman
import kotlin.time.Duration

@Zen.Module
object SlayerTimer : Feature("slayertimer", true) {
    val slayerRecord = DataUtils("slayerRecords", JsonObject())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Slayer timer", "Logs your time to kill slayer bosses to chat.", "Slayers", xyz.meowing.zen.ui.ConfigElement(
                "slayertimer",
                ElementType.Switch(false)
            ))

        return configUI
    }

    fun sendTimerMessage(action: String, timeTaken: Duration, ticks: Int) {
        val seconds = timeTaken.millis / 1000.0
        val serverTime = ticks / 20.0
        val content = "$prefix §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeTaken}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"

        ChatUtils.addMessage(content, hoverText)
        if (action == "You killed your boss") {
            val lastRecord = getSelectedSlayerRecord()

            if (timeTaken.millis < lastRecord && bossType.isNotEmpty()) {
                if (lastRecord == Long.MAX_VALUE) {
                    ChatUtils.addMessage("$prefix §d§lNew personal best! §r§7This is your first recorded time!", hoverText)
                } else {
                    ChatUtils.addMessage("$prefix §d§lNew personal best! §r§7${"%.2f".format(lastRecord / 1000.0)}s §r➜ §a${"%.2f".format(seconds)}s", hoverText)
                }

                slayerRecord.setData(slayerRecord.getData().apply {
                    addProperty("timeToKill${bossType.replace(" ", "_")}MS", timeTaken.millis)
                })
                slayerRecord.save()
            }
        }
    }

    fun getSelectedSlayerRecord(): Long {
        val data = slayerRecord.getData()
        return data.get("timeToKill${bossType.replace(" ", "_")}MS")?.asLong ?: Long.MAX_VALUE
    }

    fun sendBossSpawnMessage(timeSinceQuestStart: Duration) {
        val content = "$prefix §fBoss spawned after §b${"%.2f".format(timeSinceQuestStart.millis / 1000.0)}s"
        val hoverText = "§c${timeSinceQuestStart}ms"
        ChatUtils.addMessage(content, hoverText)
    }
}


@Zen.Command
object SlayerPBCommand : Commodore("zenslayers", "zenpb") {
    init {
        runs {
            val data = SlayerTimer.slayerRecord.getData()
            if (data.entrySet().isEmpty()) {
                ChatUtils.addMessage("$prefix §fYou have no recorded slayer boss kills.")
                return@runs
            }

            // Parse records into structured objects
            val records = data.entrySet().mapNotNull { (key, value) ->
                val raw = key.removePrefix("timeToKill").removeSuffix("MS")
                val parts = raw.split("_")
                if (parts.size < 2) return@mapNotNull null
                val slayerName = parts.dropLast(1).joinToString(" ")
                val tierRoman = parts.last()
                val tier = decodeRoman(tierRoman)
                val seconds = value.asLong / 1000.0
                Triple(slayerName, "$slayerName $tierRoman", seconds to tier)
            }

            // Group by slayer name and sort tiers
            val grouped = records.groupBy { it.first }
            ChatUtils.addMessage("$prefix §d§lYour Slayer Personal Bests:")
            for ((slayer, entries) in grouped) {
                ChatUtils.addMessage("")
                ChatUtils.addMessage("§8» §b§l$slayer Slayer")
                for ((_, displayName, timeTier) in entries.sortedBy { it.third.second }) {
                    val (seconds, _) = timeTier
                    ChatUtils.addMessage(
                        "   §7▪ §3$displayName §7➜ §b${"%.2f".format(seconds)}s"
                    )
                }
            }
        }
    }
}