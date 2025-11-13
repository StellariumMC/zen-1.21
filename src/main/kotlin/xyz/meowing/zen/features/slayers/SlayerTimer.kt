package xyz.meowing.zen.features.slayers

import com.google.gson.JsonObject
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.api.slayer.SlayerTracker.bossType
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.utils.Utils.decodeRoman
import kotlin.time.Duration

@Module
object SlayerTimer : Feature(
    "slayerTimer",
    true
) {
    private val slayerData = StoredFile("features/SlayerTimer")
    var slayerRecord: JsonObject by slayerData.jsonObject("records", JsonObject())

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Slayer timer",
                "Logs your time to kill slayer bosses to chat.",
                "Slayers",
                ConfigElement(
                    "slayerTimer",
                    ElementType.Switch(false)
                )
            )
    }

    fun sendTimerMessage(action: String, timeTaken: Duration, ticks: Int) {
        val seconds = timeTaken.millis / 1000.0
        val serverTime = ticks / 20.0
        val content = "$prefix §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeTaken.millis}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"

        KnitChat.fakeMessage(KnitText.literal(content).onHover(hoverText))
        if (action == "You killed your boss") {
            val lastRecord = getSelectedSlayerRecord()

            if (timeTaken.millis < lastRecord && bossType.isNotEmpty()) {
                val message = if (lastRecord == Long.MAX_VALUE) {
                    "$prefix §d§lNew personal best! §r§7This is your first recorded time!"
                } else {
                    "$prefix §d§lNew personal best! §r§7${"%.2f".format(lastRecord / 1000.0)}s §r➜ §a${"%.2f".format(seconds)}s"
                }

                KnitChat.fakeMessage(KnitText.literal(message).onHover(hoverText))

                slayerRecord = slayerRecord.deepCopy().apply {
                    addProperty("timeToKill${bossType.replace(" ", "_")}MS", timeTaken.millis)
                }
            }
        }
    }

    fun getSelectedSlayerRecord(): Long {
        return slayerRecord.get("timeToKill${bossType.replace(" ", "_")}MS")?.asLong ?: Long.MAX_VALUE
    }

    fun sendBossSpawnMessage(timeSinceQuestStart: Duration) {
        val content = "$prefix §fBoss spawned after §b${"%.2f".format(timeSinceQuestStart.millis / 1000.0)}s"
        val hoverText = "§c${timeSinceQuestStart}ms"
        KnitChat.fakeMessage(KnitText.literal(content).onHover(hoverText))
    }
}

@Command
object SlayerPBCommand : Commodore("zenslayers", "zenpb") {
    init {
        runs {
            val data = SlayerTimer.slayerRecord
            if (data.entrySet().isEmpty()) {
                KnitChat.fakeMessage("$prefix §fYou have no recorded slayer boss kills.")
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
            KnitChat.fakeMessage("$prefix §d§lYour Slayer Personal Bests:")
            for ((slayer, entries) in grouped) {
                KnitChat.fakeMessage("")
                KnitChat.fakeMessage("§8» §b§l$slayer Slayer")
                for ((_, displayName, timeTier) in entries.sortedBy { it.third.second }) {
                    val (seconds, _) = timeTier
                    KnitChat.fakeMessage("   §7▪ §3$displayName §7➜ §b${"%.2f".format(seconds)}s")
                }
            }
        }
    }
}