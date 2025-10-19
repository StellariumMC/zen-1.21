package xyz.meowing.zen.features.slayers.carrying

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.ClientTick
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.NetworkUtils
import xyz.meowing.zen.utils.SimpleTimeMark
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.sound.SoundEvents
import net.minecraft.text.ClickEvent
import xyz.meowing.zen.ui.ConfigMenuManager
import java.awt.Color
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object CarryCounter : Feature("carrycounter") {
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private val playerDead = Pattern.compile("^ ☠ (\\w+) was killed by (.+)\\.$")
    private val bossNames = setOf("Voidgloom Seraph", "Revenant Horror", "Tarantula Broodfather", "Sven Packmaster", "Inferno Demonlord")
    private val carryeesByBossId = ConcurrentHashMap<Int, Carryee>()
    private val completedCarriesMap = ConcurrentHashMap<String, CompletedCarry>()
    private val bossPerHourCache = ConcurrentHashMap<String, Pair<String, SimpleTimeMark>>()
    private var lasttradeuser: String? = null
    inline val carryees get() = carryeesByName.values.toList()
    val dataUtils = DataUtils("carrylogs", CarryLogs())
    val carryeesByName = ConcurrentHashMap<String, Carryee>()

    private val carrycountsend by ConfigDelegate<Boolean>("carrycountsend")
    private val carrysendmsg by ConfigDelegate<Boolean>("carrysendmsg")
    private val carryvalue by ConfigDelegate<String>("carryvalue")
    private val carrybosshighlight by ConfigDelegate<Boolean>("carrybosshighlight")
    private val carrybosscolor by ConfigDelegate<Color>("carrybosscolor")
    private val carryclienthighlight by ConfigDelegate<Boolean>("carryclienthighlight")
    private val carryclientcolor by ConfigDelegate<Color>("carryclientcolor")
    private val carrywebhook by ConfigDelegate<String>("carrywebhookurl")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Carrying", "Carry counter", "Slayers", xyz.meowing.zen.ui.ConfigElement(
                "carrycounter",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Use the command §c/carry help §rto see all the commands available. §7§oAlias: /zencarry help", "", "", xyz.meowing.zen.ui.ConfigElement(
                "",
                ElementType.TextParagraph("Use the command §c/carry help §rto see all the commands available. §7§oAlias: /zencarry help")
            ))
            .addFeatureOption("Send count", "", "QOL", xyz.meowing.zen.ui.ConfigElement(
                "carrycountsend",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Send boss spawn message", "", "QOL", xyz.meowing.zen.ui.ConfigElement(
                "carrysendmsg",
                ElementType.Switch(true)
            ))
            .addFeatureOption("Carry value", "", "QOL", xyz.meowing.zen.ui.ConfigElement(
                "carryvalue",
                ElementType.TextInput("1.3", "1.3")
            ))
            .addFeatureOption("Carry webhook URL", "", "QOL", xyz.meowing.zen.ui.ConfigElement(
                "carrywebhookurl",
                ElementType.TextInput("", "None")
            ))
            .addFeatureOption("Boss highlight", "", "Boss", xyz.meowing.zen.ui.ConfigElement(
                "carrybosshighlight",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Boss color", "", "Boss", xyz.meowing.zen.ui.ConfigElement(
                "carrybosscolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addFeatureOption("Client highlight", "", "Client", xyz.meowing.zen.ui.ConfigElement(
                "carryclienthighlight",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Client color", "", "Client", xyz.meowing.zen.ui.ConfigElement(
                "carryclientcolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))

        return configUI
    }

    override fun initialize() {
        setupLoops {
            loop<ClientTick>(200) {
                val world = world ?: return@loop
                val deadCarryees = carryeesByBossId.entries.mapNotNull { (bossId, carryee) ->
                    val entity = world.getEntityById(bossId)
                    if (entity == null || !entity.isAlive) carryee else null
                }

                deadCarryees.forEach {
                    it.reset()
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()

            tradeInit.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    lasttradeuser = matcher.group(1)
                    return@register
                }
            }

            tradeComp.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    val coins = matcher.group(1).toDoubleOrNull() ?: return@let
                    val carry = carryvalue.split(',')
                        .mapNotNull { it.trim().toDoubleOrNull() }
                        .find { abs(coins / it - round(coins / it)) < 1e-6 } ?: return@let
                    val count = round(coins / carry).toInt()
                    lasttradeuser?.let { user ->
                        ChatUtils.addMessage(
                            "$prefix §fAdd §b$user §ffor §b$count §fcarries? ",
                            "§aAdd",
                            ClickEvent.Action.RUN_COMMAND,
                            "/zencarry add $user $count"
                        )
                    }
                }
            }

            playerDead.matcher(text).let { matcher ->
                if (matcher.matches() && matcher.group(2) in bossNames) {
                    carryeesByName[matcher.group(1)]?.reset()
                }
            }
        }

        createCustomEvent<EntityEvent.Metadata>("entityMetadata") { event ->
            val name = event.name
            if (name.contains("Spawned by")) {
                val hasBlackhole = event.entity.let { entity ->
                    world?.entities?.any { Entity ->
                        entity.distanceTo(Entity) <= 3f && Entity.customName?.string?.removeFormatting()?.lowercase()?.contains("black hole") == true
                    }
                } ?: false

                if (hasBlackhole) return@createCustomEvent
                val spawnerName = name.substringAfter("by: ")
                carryeesByName[spawnerName]?.onSpawn(event.packet.id - 3)
            }
        }

        createCustomEvent<EntityEvent.Death>("entityDeath") { event ->
            carryeesByBossId[event.entity.id]?.let {
                val seconds = (it.startTime.since.millis / 1000.0)
                ChatUtils.addMessage("$prefix §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(seconds)}s")
                it.onDeath()
            }
        }

        createCustomEvent<RenderEvent.EntityGlow>("bossGlow") { event ->
            if (!carrybosshighlight) return@createCustomEvent
            carryeesByBossId[event.entity.id]?.let {
                if (player?.canSee(event.entity) == false) return@let
                event.shouldGlow = true
                event.glowColor = carrybosscolor.toColorInt()
            }
        }

        createCustomEvent<RenderEvent.EntityGlow>("clientGlow") { event ->
            if (!carryclienthighlight) return@createCustomEvent
            val cleanName = event.entity.name.string.removeFormatting()
            carryeesByName[cleanName]?.let {
                if (player?.canSee(event.entity) == false) return@let
                event.shouldGlow = true
                event.glowColor = carryclientcolor.toColorInt()
            }
        }

        CarryHUD.initialize()
        register<RenderEvent.HUD> { CarryHUD.renderHUD(it.context) }
    }

    private fun loadCompletedCarries() {
        try {
            val carriesList = dataUtils.getData().completedCarries
            completedCarriesMap.clear()
            carriesList.forEach { carry ->
                completedCarriesMap[carry.playerName] = carry
            }
            LOGGER.info("Data loaded.")
        } catch (e: Exception) {
            LOGGER.error("Data error: $e")
        }
    }

    private fun ensureDataLoaded() {
        if (completedCarriesMap.isEmpty()) loadCompletedCarries()
    }

    private fun checkRegistration() {
        if (carryeesByName.isNotEmpty()) {
            registerEvent("entityMetadata")
            registerEvent("entityDeath")
            registerEvent("bossGlow")
            registerEvent("clientGlow")
        } else {
            unregisterEvent("entityMetadata")
            unregisterEvent("entityDeath")
            unregisterEvent("bossGlow")
            unregisterEvent("clientGlow")
        }
        CarryHUD.checkRegistration()
    }

    fun addCarryee(name: String, total: Int): Carryee? {
        if (name.isBlank() || total <= 0) return null
        val existing = carryeesByName[name]
        if (existing != null) {
            existing.total += total
            return existing
        }

        val carryee = Carryee(name, total)
        carryeesByName[name] = carryee
        checkRegistration()
        return carryee
    }

    fun removeCarryee(name: String): Boolean {
        if (name.isBlank()) return false
        val carryee = carryeesByName.remove(name) ?: return false
        carryee.bossID?.let { carryeesByBossId.remove(it) }
        checkRegistration()
        return true
    }

    fun findCarryee(name: String): Carryee? = if (name.isBlank()) null else carryeesByName[name]

    fun clearCarryees() {
        carryeesByName.clear()
        carryeesByBossId.clear()
        checkRegistration()
    }

    data class CarryLogs(val completedCarries: MutableList<CompletedCarry> = mutableListOf())

    data class CompletedCarry(
        val playerName: String,
        val totalCarries: Int,
        val lastKnownCount: Int = 0,
        var timestamp: Long
    )

    data class Carryee(
        val name: String,
        var total: Int,
        var count: Int = 0,
        var lastBossTime: SimpleTimeMark = TimeUtils.zero,
        var firstBossTime: SimpleTimeMark = TimeUtils.zero,
        var startTime: SimpleTimeMark = TimeUtils.zero,
        var startTicks: Long? = null,
        var isFighting: Boolean = false,
        var bossID: Int? = null,
        var sessionStartTime: SimpleTimeMark = TimeUtils.now,
        var totalCarryTime: Long = 0,
        val bossTimes: MutableList<Long> = mutableListOf()
    ) {
        fun onSpawn(id: Int) {
            if (startTime.isZero && !isFighting) {
                startTime = TimeUtils.now
                isFighting = true
                bossID = id
                carryeesByBossId[id] = this
                Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
                showTitle("§bBoss spawned", "§bby §c$name", 1000)
                if (carrysendmsg) ChatUtils.addMessage("$prefix §fBoss spawned by §c$name")
            }
        }

        fun onDeath() {
            if (firstBossTime.isZero) firstBossTime = TimeUtils.now
            lastBossTime = TimeUtils.now
            bossTimes.add(startTime.since.millis)
            cleanup()

            if (++count >= total) {
                complete()
                if (carrywebhook.isNotEmpty()) {
                    val completeWebhookData =
                        """
                        {
                            "content": "**Carry completed!**",
                            "embeds": [{
                                "title": "Carry Completed!",
                                "description": "Player: $name\nTotal Bosses: $total\nTotal Time: ${firstBossTime.since}",
                                "color": 16766720,
                                "timestamp": "${Instant.now()}"
                            }]
                        }
                    """.trimIndent()
                    NetworkUtils.postData(
                        url = carrywebhook,
                        body = completeWebhookData,
                        onError = { LOGGER.warn("Carry-Webhook onComplete POST failed: ${it.message}") }
                    )
                }
            } else if (carrywebhook.isNotEmpty()) {
                val webhookData =
                    """
                        {
                            "content": "Boss killed by **$name**",
                            "embeds": [{
                                "title": "Boss Killed",
                                "description": "Progress: $count/$total\nmeow :3",
                                "color": 16711680,
                                "timestamp": "${Instant.now()}"
                            }]
                        }
                    """.trimIndent()
                NetworkUtils.postData(
                    url = carrywebhook,
                    body = webhookData,
                    onError = { LOGGER.error("Carry-Webhook onKill POST failed: ${it.message}") }
                )
            }

            if (carrycountsend) ChatUtils.command("/pc $name: $count/$total")
        }

        fun reset() {
            if (firstBossTime.isZero) firstBossTime = TimeUtils.now
            lastBossTime = TimeUtils.now
            bossTimes.add(startTime.since.millis)
            cleanup()
        }

        private fun cleanup() {
            isFighting = false
            bossID?.let { carryeesByBossId.remove(it) }
            startTime = TimeUtils.zero
            startTicks = null
            bossID = null
        }

        fun getTimeSinceLastBoss(): String =
            if (lastBossTime.isZero) "§7N/A"
            else String.format("%.1fs", lastBossTime.since.millis / 1000.0)

        fun getBossPerHour(): String {
            if (count <= 2) return "N/A"
            val cacheKey = "$name-$count"
            val cached = bossPerHourCache[cacheKey]
            val now = TimeUtils.now

            if (cached != null && now - cached.second < 5.seconds) return cached.first

            val totalTime = totalCarryTime + firstBossTime.since.inWholeMilliseconds
            val result = if (totalTime > 0) "${(count / (totalTime / 3.6e6)).toInt()}/hr" else "§7N/A"
            bossPerHourCache[cacheKey] = result to now
            return result
        }

        fun complete() {
            val sessionTime = sessionStartTime.since.millis / 1000

            ensureDataLoaded()

            val updatedCarry = completedCarriesMap[name]?.let { existing ->
                CompletedCarry(
                    name,
                    existing.totalCarries + count,
                    existing.totalCarries + count,
                    TimeUtils.now.millis
                )
            } ?: CompletedCarry(name, count, count, TimeUtils.now.millis)

            completedCarriesMap[name] = updatedCarry

            val carriesList = dataUtils.getData().completedCarries
            val existingIndex = carriesList.indexOfFirst { it.playerName == name }
            if (existingIndex != -1) carriesList[existingIndex] = updatedCarry
            else carriesList.add(updatedCarry)

            dataUtils.save()
            ChatUtils.addMessage("$prefix §fCarries completed for §b$name §fin §b${sessionTime}s")
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
            showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 3000)

            carryeesByName.remove(name)
            bossID?.let { carryeesByBossId.remove(it) }
            checkRegistration()
        }
    }
}