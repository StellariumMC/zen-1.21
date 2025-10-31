@file:Suppress("UNUSED")

package xyz.meowing.zen.features

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.events.Event
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.knit.api.scheduler.TimeScheduler
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.LOGGER
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.api.dungeons.DungeonFloor
import xyz.meowing.zen.api.location.LocationAPI
import xyz.meowing.zen.api.location.SkyBlockArea
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.feature.FeatureManager

open class Feature(
    val configKey: String? = null,
    val skyblockOnly: Boolean = false,
    island: Any? = null,
    area: Any? = null,
    dungeonFloor: Any? = null
) {
    val events = mutableListOf<EventCall>()
    val tickHandles = mutableSetOf<TickScheduler.Handle>()
    val timeHandles = mutableSetOf<TimeScheduler.Handle>()
    val timerIds = mutableSetOf<Long>()
    val namedEventCalls = mutableMapOf<String, EventCall>()
    private var setupLoops: (() -> Unit)? = null
    private var isRegistered = false

    private val islands: List<SkyBlockIsland> = when (island) {
        is SkyBlockIsland -> listOf(island)
        is List<*> -> island.filterIsInstance<SkyBlockIsland>()
        else -> emptyList()
    }

    private val areas: List<SkyBlockArea> = when (area) {
        is SkyBlockArea -> listOf(area)
        is List<*> -> area.filterIsInstance<SkyBlockArea>()
        else -> emptyList()
    }

    private val dungeonFloors: List<DungeonFloor> = when (dungeonFloor) {
        is DungeonFloor -> listOf(dungeonFloor)
        is List<*> -> dungeonFloor.filterIsInstance<DungeonFloor>()
        else -> emptyList()
    }

    init {
        FeatureManager.addFeature(this)
    }

    private fun checkConfig(): Boolean {
        return try {
            configKey?.let {
                ConfigManager.getConfigValue(it) as? Boolean ?: false
            } ?: true
        } catch (e: Exception) {
            Zen.LOGGER.warn("Caught exception in checkConfig(): $e")
            false
        }
    }

    open fun initialize() {}

    protected fun setupLoops(block: () -> Unit) {
        setupLoops = block
    }

    open fun onRegister() {
        if (Debug.debugmode) LOGGER.info("$prefix §fRegistering §b$configKey")
        setupLoops?.invoke()
    }

    open fun onUnregister() {
        if (Debug.debugmode) LOGGER.info("$prefix §fUnregistering §b$configKey")
        cancelLoops()
    }

    open fun addConfig() {}

    fun isEnabled(): Boolean = checkConfig() && inSkyblock() && inArea() && inSubarea() && inDungeonFloor()

    fun update() = onToggle(isEnabled())

    @Synchronized
    open fun onToggle(state: Boolean) {
        if (state == isRegistered) return

        if (state) {
            events.forEach { it.register() }
            onRegister()
            isRegistered = true
        } else {
            events.forEach { it.unregister() }
            onUnregister()
            isRegistered = false
        }
    }

    inline fun <reified T : Event> register(priority: Int = 0, noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(priority, false, cb))
    }

    inline fun <reified T : Event> createCustomEvent(name: String, priority: Int = 0, noinline cb: (T) -> Unit) {
        val eventCall = EventBus.register<T>(priority, false, cb)
        namedEventCalls[name] = eventCall
    }

    fun registerEvent(name: String) {
        namedEventCalls[name]?.register()
    }

    fun unregisterEvent(name: String) {
        namedEventCalls[name]?.unregister()
    }

    inline fun <reified T> loop(intervalTicks: Long, noinline action: () -> Unit): Any {
        return when (T::class) {
            ClientTick::class -> {
                val handle = TickScheduler.Client.repeat(intervalTicks, action = action)
                tickHandles.add(handle)
                handle
            }
            ServerTick::class -> {
                val handle = TickScheduler.Server.repeat(intervalTicks, action = action)
                tickHandles.add(handle)
                handle
            }
            Timer::class -> {
                val handle = TimeScheduler.repeat(intervalTicks, action = action)
                timeHandles.add(handle)
                handle
            }
            else -> throw IllegalArgumentException("Unsupported loop type: ${T::class}")
        }
    }

    inline fun <reified T> loopDynamic(
        noinline delay: () -> Long,
        noinline stop: () -> Boolean = { false },
        noinline action: () -> Unit
    ): Any {
        return when (T::class) {
            Timer::class -> {
                val handle = TimeScheduler.repeatDynamic(delay, stop, action)
                timeHandles.add(handle)
                handle
            }
            ClientTick::class -> {
                val handle = TickScheduler.Client.repeatDynamic(delay, action)
                tickHandles.add(handle)
                handle
            }
            ServerTick::class -> {
                val handle = TickScheduler.Server.repeatDynamic(delay, action)
                tickHandles.add(handle)
                handle
            }
            else -> throw IllegalArgumentException("Unsupported loop type: ${T::class}")
        }
    }

    fun createTimer(ticks: Int, onTick: () -> Unit = {}, onComplete: () -> Unit = {}): Long {
        val id = TickScheduler.Client.createTimer(ticks, onTick, onComplete)
        timerIds.add(id)
        return id
    }

    fun getTimer(timerId: Long): TickScheduler.Timer? = TickScheduler.Client.getTimer(timerId)

    private fun cancelLoops() {
        tickHandles.forEach { it.cancel() }
        timeHandles.forEach { it.cancel() }
        timerIds.forEach { TickScheduler.Client.cancelTimer(it) }
        tickHandles.clear()
        timeHandles.clear()
        timerIds.clear()
    }

    fun inSkyblock(): Boolean = !skyblockOnly || LocationAPI.isOnSkyBlock

    fun inArea(): Boolean = islands.isEmpty() || LocationAPI.island in islands

    fun inSubarea(): Boolean = areas.isEmpty() || LocationAPI.area in areas

    fun inDungeonFloor(): Boolean {
        if (dungeonFloors.isEmpty()) return true
        return SkyBlockIsland.THE_CATACOMBS.inIsland() && DungeonAPI.dungeonFloor in dungeonFloors
    }

    fun hasIslands(): Boolean = islands.isNotEmpty()

    fun hasAreas(): Boolean = areas.isNotEmpty()

    fun hasDungeonFloors(): Boolean = dungeonFloors.isNotEmpty()
}

class ClientTick
class ServerTick
class Timer