package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
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
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.toColorFloat
import net.minecraft.sound.SoundEvents
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import java.awt.Color
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round

object carrycounter : Feature("carrycounter") {
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private val playerDead = Pattern.compile("^ ☠ (\\w+) was killed by (.+)\\.$")
    private val bossNames = setOf("Voidgloom Seraph", "Revenant Horror", "Tarantula Broodfather", "Sven Packmaster", "Inferno Demonlord")

    private var lasttradeuser: String? = null
    private val carryeesByName = ConcurrentHashMap<String, Carryee>()
    private val carryeesByBossId = ConcurrentHashMap<Int, Carryee>()
    private val completedCarriesMap = ConcurrentHashMap<String, CompletedCarry>()
    private val bossPerHourCache = ConcurrentHashMap<String, Pair<String, Long>>()

    val carryees get() = carryeesByName.values.toList()
    val dataUtils = DataUtils("carrylogs", CarryLogs())

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
            )
            )
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
                "Highlights your client's boss.",
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

        register<GuiEvent.Hud> { CarryHUD.renderHUD(it.context) }
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
                        val targetEntity = mc.world!!.getEntityById(packet.id)
                        val hasBlackhole = targetEntity?.let { entity ->
                            mc.world!!.entities.any { Entity ->
                                entity.distanceTo(Entity) <= 3f && Entity.customName?.string?.removeFormatting()?.lowercase()?.contains("black hole") == true
                            }
                        } ?: false

                        if (hasBlackhole) return@let
                        val spawnerName = name.substringAfter("by: ")
                        carryeesByName[spawnerName]?.onSpawn(event.packet.id - 3)
                    }
                }
            }))

            events.add(EventBus.register<EntityEvent.Leave> ({ event ->
                if (event.entity.isAlive) return@register
                carryeesByBossId[event.entity.id]?.let {
                    val ms = System.currentTimeMillis() - (it.startTime ?: 0L)
                    ChatUtils.addMessage("§c[Zen] §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(ms / 1000.0)}s")
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
            if (registered || !Zen.config.carrybosshighlight) return
            events.add(EventBus.register<RenderEvent.WorldPostEntities> ({ event ->
                val world = mc.world ?: return@register
                carryeesByBossId.forEach { bossId, _ ->
                    val entity = world.getEntityById(bossId)
                    if (entity != null && !entity.isRemoved) {
                        val entityPos = entity.getLerpedPos(event.context!!.tickCounter().getTickProgress(true))
                        val x = entityPos.x - mc.gameRenderer.camera.pos.x
                        val y = entityPos.y - mc.gameRenderer.camera.pos.y
                        val z = entityPos.z - mc.gameRenderer.camera.pos.z
                        val color = Zen.config.carrybosshighlightcolor
                        RenderUtils.renderEntityFilled(
                            event.context.matrixStack(),
                            event.context.consumers(),
                            x, y, z, entity.width + 0.5f, entity.height  + 0.25f,
                            color.red.toColorFloat(), color.green.toColorFloat(), color.blue.toColorFloat(), color.alpha.toColorFloat()
                        )
                    }
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
            if (registered || !Zen.config.carryclienthighlight) return
            events.add(EventBus.register<RenderEvent.WorldPostEntities> ({ event ->
                val world = mc.world ?: return@register

                world.players.forEach { player ->
                    val cleanName = player.name.string.removeFormatting()
                    carryeesByName[cleanName]?.let {
                        val entityPos = player.getLerpedPos(event.context!!.tickCounter().getTickProgress(true))
                        val x = entityPos.x - mc.gameRenderer.camera.pos.x
                        val y = entityPos.y - mc.gameRenderer.camera.pos.y
                        val z = entityPos.z - mc.gameRenderer.camera.pos.z
                        val color = Zen.config.carryclienthighlightcolor
                        RenderUtils.renderEntityFilled(
                            event.context.matrixStack(),
                            event.context.consumers(),
                            x, y, z, player.width, player.height,
                            color.red.toColorFloat(), color.green.toColorFloat(), color.blue.toColorFloat(), color.alpha.toColorFloat()
                        )
                    }
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
                        val carry = Zen.config.carryvalue.split(',')
                            .mapNotNull { it.trim().toDoubleOrNull() }
                            .find { abs(coins / it - round(coins / it)) < 1e-6 } ?: return@let
                        val count = round(coins / carry).toInt()
                        lasttradeuser?.let { user ->
                            ChatUtils.addMessage(
                                "§c[Zen] §fAdd §b$user §ffor §b$count §fcarries? ",
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
        var lastBossTime: Long? = null,
        var firstBossTime: Long? = null,
        var startTime: Long? = null,
        var startTicks: Long? = null,
        var isFighting: Boolean = false,
        var bossID: Int? = null,
        var sessionStartTime: Long = System.currentTimeMillis(),
        var totalCarryTime: Long = 0,
        val bossTimes: MutableList<Long> = mutableListOf()
    ) {
        fun onSpawn(id: Int) {
            if (startTime == null && !isFighting) {
                startTime = System.currentTimeMillis()
                isFighting = true
                bossID = id
                carryeesByBossId[id] = this
                Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
                Utils.showTitle("§bBoss spawned", "§bby §c$name", 10)
            }
        }

        fun onDeath() {
            if (firstBossTime == null) firstBossTime = System.currentTimeMillis()
            lastBossTime = System.currentTimeMillis()
            startTime?.let { bossTimes.add(System.currentTimeMillis() - it) }
            cleanup()
            if (++count >= total) complete()
            if (Zen.config.carrycountsend) ChatUtils.command("/pc $name: $count/$total")
        }

        fun reset() {
            if (firstBossTime == null) firstBossTime = System.currentTimeMillis()
            lastBossTime = System.currentTimeMillis()
            startTime?.let { bossTimes.add(System.currentTimeMillis() - it) }
            cleanup()
        }

        private fun cleanup() {
            isFighting = false
            bossID?.let { carryeesByBossId.remove(it) }
            startTime = null
            startTicks = null
            bossID = null
        }

        fun getTimeSinceLastBoss(): String = lastBossTime?.let {
            String.format("%.1fs", (System.currentTimeMillis() - it) / 1000.0)
        } ?: "§7N/A"

        fun getBossPerHour(): String {
            if (count <= 2) return "N/A"
            val cacheKey = "$name-$count"
            val cached = bossPerHourCache[cacheKey]
            val now = System.currentTimeMillis()

            if (cached != null && now - cached.second < 5000) return cached.first

            val totalTime = totalCarryTime + (firstBossTime?.let { now - it } ?: 0)
            val result = if (totalTime > 0) "${(count / (totalTime / 3.6e6)).toInt()}/hr" else "§7N/A"
            bossPerHourCache[cacheKey] = result to now
            return result
        }

        fun complete() {
            val sessionTime = System.currentTimeMillis() - sessionStartTime

            ensureDataLoaded()

            val updatedCarry = completedCarriesMap[name]?.let { existing ->
                CompletedCarry(
                    name,
                    existing.totalCarries + count,
                    existing.totalCarries + count,
                    System.currentTimeMillis()
                )
            } ?: CompletedCarry(name, count, count, System.currentTimeMillis())

            completedCarriesMap[name] = updatedCarry

            val carriesList = dataUtils.getData().completedCarries
            val existingIndex = carriesList.indexOfFirst { it.playerName == name }
            if (existingIndex != -1) carriesList[existingIndex] = updatedCarry
            else carriesList.add(updatedCarry)

            dataUtils.save()
            ChatUtils.addMessage("§c[Zen] §fCarries completed for §b$name §fin §b${sessionTime / 1000}s")
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 5f, 2f)
            Utils.showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 20)

            carryeesByName.remove(name)
            bossID?.let { carryeesByBossId.remove(it) }
            checkRegistration()
        }
    }
}