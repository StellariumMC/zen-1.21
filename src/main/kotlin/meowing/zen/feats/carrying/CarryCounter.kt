package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.events.ChatEvent
import meowing.zen.events.GuiEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.LoopUtils
import meowing.zen.utils.SimpleTimeMark
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.TitleUtils.showTitle
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.sound.SoundEvents
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import java.awt.Color
import java.util.Optional
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

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrycounter",
                "Carry counter",
                "Counts and sends the carries that you do.",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrycountsend",
                "Send count",
                "Sends the count in party chat",
                ElementType.Switch(true)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrysendmsg",
                "Send boss spawn message",
                "Sends the boss spawn message in chat",
                ElementType.Switch(true)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrybosshighlight",
                "Carry boss highlight",
                "Highlights your client's boss.",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrybosshighlightcolor",
                "Carry boss highlight color",
                "The color for boss highlight",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["carrybosshighlight"] as? Boolean == true }
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryclienthighlight",
                "Carry client highlight",
                "Highlights your client.",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryclienthighlightcolor",
                "Carry client highlight color",
                "The color for client highlight",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["carryclienthighlight"] as? Boolean == true }
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryvalue",
                "Carry value",
                "Carry values for the mod to automatically detect in a trade",
                ElementType.TextInput("1.3", "1.3")
            ))
    }

    override fun initialize() {
        LoopUtils.loop(400) {
            val world = mc.world ?: return@loop
            val deadCarryees = carryeesByBossId.entries.mapNotNull { (bossId, carryee) ->
                val entity = world.getEntityById(bossId)
                if (entity == null || entity.isRemoved) carryee else null
            }
            deadCarryees.forEach { it.reset() }
        }

        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            tradeInit.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    lasttradeuser = matcher.group(1)
                    ChatEvents.register()
                    LoopUtils.setTimeout(2000, { ChatEvents.unregister() })
                }
            }
        }

        CarryHUD.initialize()

        register<GuiEvent.HUD> { CarryHUD.renderHUD(it.context) }
    }

    private fun loadCompletedCarries() {
        try {
            val carriesList = dataUtils.getData().completedCarries
            completedCarriesMap.clear()
            carriesList.forEach { carry ->
                completedCarriesMap[carry.playerName] = carry
            }
            println("[Zen] Data loaded.")
        } catch (e: Exception) {
            println("[Zen] Data error: $e")
        }
    }

    private fun ensureDataLoaded() {
        if (completedCarriesMap.isEmpty()) loadCompletedCarries()
    }

    private fun checkRegistration() {
        if (carryeesByName.isNotEmpty()) {
            EntityEvents.register()
            RenderBossEntity.register()
            RenderPlayerEntity.register()
        } else {
            EntityEvents.unregister()
            RenderBossEntity.unregister()
            RenderPlayerEntity.unregister()
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

    object EntityEvents {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered) return
            events.add(EventBus.register<EntityEvent.Metadata> ({ event ->
                val packet = event.packet
                packet.trackedValues?.find { it.id == 2 && it.value is Optional<*> }?.let { obj ->
                    val optional = obj.value as Optional<*>
                    val name = (optional.orElse(null) as? Text)?.string?.removeFormatting() ?: return@let
                    if (name.contains("Spawned by")) {
                        val targetEntity = world?.getEntityById(packet.id)
                        val hasBlackhole = targetEntity?.let { entity ->
                            world?.entities?.any { Entity ->
                                entity.distanceTo(Entity) <= 3f && Entity.customName?.string?.removeFormatting()?.lowercase()?.contains("black hole") == true
                            }
                        } ?: false

                        if (hasBlackhole) return@let
                        val spawnerName = name.substringAfter("by: ")
                        carryeesByName[spawnerName]?.onSpawn(event.packet.id - 3)
                    }
                }
            }))

            events.add(EventBus.register<EntityEvent.Death> ({ event ->
                carryeesByBossId[event.entity.id]?.let {
                    val seconds = (it.startTime.since.millis / 1000.0)
                    ChatUtils.addMessage("$prefix §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(seconds)}s")
                    it.onDeath()
                }
            }))
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    object RenderBossEntity {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered || !carrybosshighlight) return
            events.add(EventBus.register<RenderEvent.EntityGlow> ({ event ->
                carryeesByBossId[event.entity.id]?.let {
                    if (player?.canSee(event.entity) == false) return@let
                    event.shouldGlow = true
                    event.glowColor = carrybosscolor.toColorInt()
                }
            }))
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    object RenderPlayerEntity {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered || !carryclienthighlight) return
            events.add(EventBus.register<RenderEvent.EntityGlow> ({ event ->
                val cleanName = event.entity.name.string.removeFormatting()
                carryeesByName[cleanName]?.let {
                    if (player?.canSee(event.entity) == false) return@let
                    event.shouldGlow = true
                    event.glowColor = carryclientcolor.toColorInt()
                }
            }))
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    object ChatEvents {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered) return
            events.add(EventBus.register<ChatEvent.Receive> ({ event ->
                val text = event.message.string.removeFormatting()

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
                        return@register
                    }
                }

                playerDead.matcher(text).let { matcher ->
                    if (matcher.matches() && matcher.group(2) in bossNames) {
                        carryeesByName[matcher.group(1)]?.reset()
                    }
                }

                lasttradeuser = null
                LoopUtils.setTimeout(500) { unregister() }
            }))
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
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
            if (++count >= total) complete()
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
            val sessionTime = sessionStartTime.since

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
            ChatUtils.addMessage("$prefix §fCarries completed for §b$name §fin §b${sessionTime / 1000}s")
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
            showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 3000)

            carryeesByName.remove(name)
            bossID?.let { carryeesByBossId.remove(it) }
            checkRegistration()
        }
    }
}