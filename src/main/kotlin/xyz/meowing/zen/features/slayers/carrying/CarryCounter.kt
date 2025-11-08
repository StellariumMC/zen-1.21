package xyz.meowing.zen.features.slayers.carrying

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.ClientTick
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.NetworkUtils
import xyz.meowing.zen.utils.SimpleTimeMark
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils
import net.minecraft.sound.SoundEvents
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.knit.api.text.core.ClickEvent
import xyz.meowing.zen.Zen.LOGGER
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds

@Module
object CarryCounter : Feature(
    "carryCounter",
    true
) {
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private val playerDead = Pattern.compile("^ ☠ (\\w+) was killed by (.+)\\.$")
    private val bossNames = setOf("Voidgloom Seraph", "Revenant Horror", "Tarantula Broodfather", "Sven Packmaster", "Inferno Demonlord")
    private val carriesByBossId = ConcurrentHashMap<Int, Carry>()
    private val completedCarriesMap = ConcurrentHashMap<String, CompletedCarry>()
    private val bossPerHourCache = ConcurrentHashMap<String, Pair<String, SimpleTimeMark>>()
    private var lastTradedUser: String? = null
    inline val carries get() = carriesByName.values.toList()

    private val carryData = StoredFile("features/CarryCounter")
    var completedCarries: List<CompletedCarry> by carryData.list("completedCarries", CompletedCarry.CODEC, emptyList())
    val carriesByName = ConcurrentHashMap<String, Carry>()

    private val carryCountSend by ConfigDelegate<Boolean>("carryCounter.countSend")
    private val carrySendMsg by ConfigDelegate<Boolean>("carryCounter.sendMsg")
    private val carryValue by ConfigDelegate<String>("carryCounter.value")
    private val carryBossHighlight by ConfigDelegate<Boolean>("carryCounter.bossHighlight")
    private val carryBossColor by ConfigDelegate<Color>("carryCounter.bossColor")
    private val carryClientHighlight by ConfigDelegate<Boolean>("carryCounter.clientHighlight")
    private val carryClientColor by ConfigDelegate<Color>("carryCounter.clientColor")
    private val carryWebhook by ConfigDelegate<String>("carryCounter.webhookUrl")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Carry counter",
                "Track and manage carries",
                "Slayers",
                ConfigElement(
                    "carryCounter",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Help",
                ConfigElement(
                    "carryCounter.help",
                    ElementType.TextParagraph("Use the command §c/carry help §rto see all the commands available. §7§oAlias: /zencarry help")
                )
            )
            .addFeatureOption(
                "Send count",
                ConfigElement(
                    "carryCounter.countSend",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Send boss spawn message",
                ConfigElement(
                    "carryCounter.sendMsg",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Carry value",
                ConfigElement(
                    "carryCounter.value",
                    ElementType.TextInput("1.3", "1.3")
                )
            )
            .addFeatureOption(
                "Carry webhook URL",
                ConfigElement(
                    "carryCounter.webhookUrl",
                    ElementType.TextInput("", "None")
                )
            )
            .addFeatureOption(
                "Boss highlight",
                ConfigElement(
                    "carryCounter.bossHighlight",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Boss color",
                ConfigElement(
                    "carryCounter.bossColor",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
            .addFeatureOption(
                "Client highlight",
                ConfigElement(
                    "carryCounter.clientHighlight",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Client color",
                ConfigElement(
                    "carryCounter.clientColor",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }

    override fun initialize() {
        setupLoops {
            loop<ClientTick>(200) {
                val world = world ?: return@loop
                val deadCarries = carriesByBossId.entries.mapNotNull { (bossId, carry) ->
                    val entity = world.getEntityById(bossId)
                    if (entity == null || !entity.isAlive) carry else null
                }

                deadCarries.forEach {
                    it.reset()
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val text = event.message.string.removeFormatting()

            tradeInit.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    lastTradedUser = matcher.group(1)
                    return@register
                }
            }

            tradeComp.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    val coins = matcher.group(1).toDoubleOrNull() ?: return@let
                    val carry = carryValue.split(',')
                        .mapNotNull { it.trim().toDoubleOrNull() }
                        .find { abs(coins / it - round(coins / it)) < 1e-6 } ?: return@let
                    val count = round(coins / carry).toInt()
                    lastTradedUser?.let { user ->
                        val message = KnitText
                            .literal("$prefix §fClick here to add §b$user §ffor §b$count §fcarries")
                            .onClick(ClickEvent.RunCommand("/zencarry add $user $count"))
                            .toVanilla()

                        KnitChat.fakeMessage(message)
                    }
                }
            }

            playerDead.matcher(text).let { matcher ->
                if (matcher.matches() && matcher.group(2) in bossNames) {
                    carriesByName[matcher.group(1)]?.reset()
                }
            }
        }

        createCustomEvent<EntityEvent.Packet.Metadata>("entityMetadata") { event ->
            val name = event.name
            if (name.contains("Spawned by")) {
                val hasBlackhole = event.entity.let { entity ->
                    world?.entities?.any { worldEnt ->
                        entity.distanceTo(worldEnt) <= 3f && worldEnt.customName?.string?.removeFormatting()?.lowercase()?.contains("black hole") == true
                    }
                } ?: false

                if (hasBlackhole) return@createCustomEvent
                val spawnerName = name.substringAfter("by: ")
                carriesByName[spawnerName]?.onSpawn(event.packet.id - 3)
            }
        }

        createCustomEvent<EntityEvent.Death>("entityDeath") { event ->
            carriesByBossId[event.entity.id]?.let {
                val seconds = (it.startTime.since.millis / 1000.0)
                KnitChat.fakeMessage("$prefix §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(seconds)}s")
                it.onDeath()
            }
        }

        createCustomEvent<RenderEvent.Entity.Pre>("bossGlow") { event ->
            if (!carryBossHighlight) return@createCustomEvent
            val entity = event.entity

            carriesByBossId[entity.id]?.let {
                if (player?.canSee(entity) == false) return@let
                entity.glowThisFrame = true
                entity.glowingColor = carryBossColor.rgb
            }
        }

        createCustomEvent<RenderEvent.Entity.Pre>("clientGlow") { event ->
            if (!carryClientHighlight) return@createCustomEvent
            val entity = event.entity
            val cleanName = entity.name.string.removeFormatting()

            carriesByName[cleanName]?.let {
                if (player?.canSee(entity) == false) return@let
                entity.glowThisFrame = true
                entity.glowingColor = carryClientColor.rgb
            }
        }

        register<GuiEvent.Render.HUD> { event ->
            if (carries.isEmpty()) return@register
            val context = event.context

            if (event.renderType != GuiEvent.RenderType.Pre && client.currentScreen == null) {
                CarryHUD.renderHUD(context)
            } else {
                CarryHUD.renderInventoryHUD(context)
            }
        }

        register<GuiEvent.Click> { event ->
            if (carries.isEmpty() || !event.buttonState || event.mouseButton != 0) return@register
            CarryHUD.onMouseInput()
        }
    }

    private fun loadCompletedCarries() {
        try {
            completedCarriesMap.clear()
            completedCarries.forEach { carry ->
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
        if (carriesByName.isNotEmpty()) {
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
    }

    fun addCarry(name: String, total: Int): Carry? {
        if (name.isBlank() || total <= 0) return null
        val existing = carriesByName[name]
        if (existing != null) {
            existing.total += total
            return existing
        }

        val carry = Carry(name, total)
        carriesByName[name] = carry
        checkRegistration()
        return carry
    }

    fun removeCarry(name: String): Boolean {
        if (name.isBlank()) return false
        val carryee = carriesByName.remove(name) ?: return false
        carryee.bossID?.let { carriesByBossId.remove(it) }
        checkRegistration()
        return true
    }

    fun findCarry(name: String): Carry? = if (name.isBlank()) null else carriesByName[name]

    fun clearCarries() {
        carriesByName.clear()
        carriesByBossId.clear()
        checkRegistration()
    }

    data class CompletedCarry(
        val playerName: String,
        val totalCarries: Int,
        val lastKnownCount: Int = 0,
        var timestamp: Long
    ) {
        companion object {
            val CODEC: Codec<CompletedCarry> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("playerName").forGetter { it.playerName },
                    Codec.INT.fieldOf("totalCarries").forGetter { it.totalCarries },
                    Codec.INT.optionalFieldOf("lastKnownCount", 0).forGetter { it.lastKnownCount },
                    Codec.LONG.fieldOf("timestamp").forGetter { it.timestamp }
                ).apply(instance, ::CompletedCarry)
            }
        }
    }

    data class Carry(
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
                carriesByBossId[id] = this
                Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
                showTitle("§bBoss spawned", "§bby §c$name", 1000)
                if (carrySendMsg) KnitChat.fakeMessage("$prefix §fBoss spawned by §c$name")
            }
        }

        fun onDeath() {
            if (firstBossTime.isZero) firstBossTime = TimeUtils.now
            lastBossTime = TimeUtils.now
            bossTimes.add(startTime.since.millis)
            cleanup()

            if (++count >= total) {
                complete()
                if (carryWebhook.isNotEmpty()) {
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
                        url = carryWebhook,
                        body = completeWebhookData,
                        onError = { LOGGER.warn("Carry-Webhook onComplete POST failed: ${it.message}") }
                    )
                }
            } else if (carryWebhook.isNotEmpty()) {
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
                    url = carryWebhook,
                    body = webhookData,
                    onError = { LOGGER.error("Carry-Webhook onKill POST failed: ${it.message}") }
                )
            }

            if (carryCountSend) KnitChat.sendCommand("pc $name: $count/$total")
        }

        fun reset() {
            if (firstBossTime.isZero) firstBossTime = TimeUtils.now
            lastBossTime = TimeUtils.now
            bossTimes.add(startTime.since.millis)
            cleanup()
        }

        private fun cleanup() {
            isFighting = false
            bossID?.let { carriesByBossId.remove(it) }
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

            val carriesList = completedCarries.toMutableList()
            val existingIndex = carriesList.indexOfFirst { it.playerName == name }
            if (existingIndex != -1) carriesList[existingIndex] = updatedCarry
            else carriesList.add(updatedCarry)

            completedCarries = carriesList

            KnitChat.fakeMessage("$prefix §fCarries completed for §b$name §fin §b${sessionTime}s")
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
            showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 3000)

            carriesByName.remove(name)
            bossID?.let { carriesByBossId.remove(it) }
            checkRegistration()
        }
    }
}