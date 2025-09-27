package meowing.zen.events

import meowing.zen.Zen.Companion.configUI
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.ScoreboardUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.util.ActionResult
import org.lwjgl.glfw.GLFW
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val messages = mutableListOf<String>()
    val listeners = ConcurrentHashMap<Class<*>, MutableSet<PrioritizedCallback<*>>>()
    data class PrioritizedCallback<T>(val priority: Int, val callback: (T) -> Unit)

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            post(TickEvent.Client())
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            post(EntityEvent.Join(entity))
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            post(EntityEvent.Leave(entity))
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            if(WorldEvent.Change.shouldPost()) {
                post(WorldEvent.Change(world))
            }
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { msg, show ->
            val customEvent = when (show) {
                true -> GameEvent.ActionBar(msg)
                false -> ChatEvent.Receive(msg)
            }

            !post(customEvent)
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { string ->
            val fromChatUtils = messages.remove(string)
            !post(ChatEvent.Send(string, fromChatUtils))
        }

        ClientSendMessageEvents.ALLOW_COMMAND.register { string ->
            val command = string.split(" ")[0].lowercase()
            val commandString = "/$string"
            val fromChatUtils = messages.remove(commandString)

            when (command) {
                "gc", "pc", "ac", "msg", "tell", "r", "say", "w", "reply" -> {
                    !post(ChatEvent.Send(commandString, fromChatUtils))
                }
                else -> true
            }
        }

        WorldRenderEvents.LAST.register { context ->
            post(RenderEvent.World(context))
        }

        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            post(RenderEvent.WorldPostEntities(context))
        }

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, mx, my, mbtn ->
                !post(GuiEvent.Click(mx, my, mbtn, true, screen))
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, mx, my, mbtn ->
                !post(GuiEvent.Click(mx, my, mbtn, false, screen))
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scancode, modifiers ->
                val charTyped = GLFW.glfwGetKeyName(key, scancode)?.firstOrNull() ?: '\u0000'
                !post(GuiEvent.Key(GLFW.glfwGetKeyName(key, scancode), key, charTyped, scancode, screen))
            }

            GLFW.glfwSetCharCallback(mc.window.handle) { window, codepoint ->
                val charTyped = codepoint.toChar()

                !post(GuiEvent.Key(null, GLFW.GLFW_KEY_UNKNOWN, charTyped, 0, screen))
            }

            ScreenEvents.afterRender(screen).register { _, context, mouseX, mouseY, tickDelta ->
                post(GuiEvent.AfterRender(screen, context))
            }
        }

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            if (screen != null) post(GuiEvent.Open(screen))
        }

        WorldRenderEvents.BLOCK_OUTLINE.register { worldContext, blockContext ->
            !post(RenderEvent.BlockOutline(worldContext, blockContext))
        }

        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            post(GameEvent.Load())
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            post(GameEvent.Unload())
        }

        UseItemCallback.EVENT.register { player, world, hand ->
            post(EntityEvent.Interact(player, world, hand, "USE_ITEM"))
            ActionResult.PASS
        }

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            post(EntityEvent.Interact(player, world, hand, "USE_BLOCK", hitResult.blockPos))
            ActionResult.PASS
        }

        UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            post(EntityEvent.Interact(player, world, hand, "USE_ENTITY"))
            ActionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            post(EntityEvent.Interact(player, world, hand, "ATTACK_BLOCK", pos))
            ActionResult.PASS
        }

        AttackEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            post(EntityEvent.Interact(player, world, hand, "ATTACK_ENTITY"))
            ActionResult.PASS
        }

        ItemTooltipCallback.EVENT.register { stack, context, type, lines ->
            val tooltipEvent = ItemTooltipEvent(stack, context, type, lines)
            post(tooltipEvent)
            if (tooltipEvent.lines != lines) {
                lines.clear()
                lines.addAll(tooltipEvent.lines)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            post(GameEvent.Disconnect())
        }
    }

    fun onPacketReceived(packet: Packet<*>): Boolean {
        if (post(PacketEvent.Received(packet))) return true

        return when (packet) {
            is CommonPingS2CPacket -> {
                post(TickEvent.Server())
            }
            is EntitySpawnS2CPacket -> {
                post(EntityEvent.Spawn(packet))
            }
            is ScoreboardObjectiveUpdateS2CPacket, is ScoreboardScoreUpdateS2CPacket, is ScoreboardDisplayS2CPacket, is TeamS2CPacket -> {
                val lines = ScoreboardUtils.getSidebarLines()
                post(SidebarUpdateEvent(lines))
            }
            is PlayerListS2CPacket -> {
                when (packet.actions.firstOrNull()) {
                    PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME -> {
                        post(TablistEvent.Update(packet))
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    fun onPacketSent(packet: Packet<*>) {
        post(PacketEvent.Sent(packet))
    }

    /*
     * Modified from Devonian code
     * Under GPL 3.0 License
     */
    inline fun <reified T : Event> register(priority: Int = 0, noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        val eventClass = T::class.java
        val handlers = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
        val prioritizedCallback = PrioritizedCallback(priority, callback)
        if (add) handlers.add(prioritizedCallback)
        return EventCallImpl(prioritizedCallback, handlers)
    }

    inline fun <reified T : Event> register(noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        return register(0, callback, add)
    }

    inline fun <reified T : Event> register(noinline callback: (T) -> Unit): EventCall {
        return register(0, callback, true)
    }

    @JvmStatic
    fun <T : Event> registerJava(eventClass: Class<T>, priority: Int, add: Boolean = false, callback: (T) -> Unit): EventCall {
        val handlers = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
        val prioritizedCallback = PrioritizedCallback(priority, callback)
        if (add) handlers.add(prioritizedCallback)
        return EventCallImpl(prioritizedCallback, handlers)
    }

    fun <T : Event> post(event: T): Boolean {
        val eventClass = event::class.java
        val handlers = listeners[eventClass] ?: return false
        if (handlers.isEmpty()) return false

        val sortedHandlers = handlers.sortedBy { it.priority }

        for (handler in sortedHandlers) {
            try {
                @Suppress("UNCHECKED_CAST")
                (handler.callback as (T) -> Unit)(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (event is CancellableEvent) event.isCancelled() else false
    }

    class EventCallImpl(
        private val callback: PrioritizedCallback<*>,
        private val handlers: MutableSet<PrioritizedCallback<*>>
    ) : EventCall {
        override fun unregister(): Boolean = handlers.remove(callback)
        override fun register(): Boolean = handlers.add(callback)
    }

    interface EventCall {
        fun unregister(): Boolean
        fun register(): Boolean
    }
}

inline fun <reified T : Event> configRegister(
    configKeys: Any,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline enabledCheck: (Map<String, Any?>) -> Boolean,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    val eventCall = EventBus.register<T>(priority, callback, false)
    val keys = when (configKeys) {
        is String -> listOf(configKeys)
        is List<*> -> configKeys.filterIsInstance<String>()
        else -> throw IllegalArgumentException("configKeys must be String or List<String>")
    }

    val areas = when (area) {
        is String -> listOf(area.lowercase())
        is List<*> -> area.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }
    val subareas = when (subarea) {
        is String -> listOf(subarea.lowercase())
        is List<*> -> subarea.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }

    val checkAndUpdate = {
        val configValues = keys.associateWith { configUI.getConfigValue(it) }
        val configEnabled = enabledCheck(configValues)
        val skyblockEnabled = !skyblockOnly || LocationUtils.inSkyblock
        val areaEnabled = areas.isEmpty() || areas.any { LocationUtils.checkArea(it) }
        val subareaEnabled = subareas.isEmpty() || subareas.any { LocationUtils.checkSubarea(it) }

        if (configEnabled && skyblockEnabled && areaEnabled && subareaEnabled) {
            eventCall.register()
        } else {
            eventCall.unregister()
        }
    }

    keys.forEach { configKey ->
        configUI.registerListener(configKey) { checkAndUpdate() }
    }

    if (areas.isNotEmpty()) {
        EventBus.register<AreaEvent.Main> { checkAndUpdate() }
    }

    if (subareas.isNotEmpty()) {
        EventBus.register<AreaEvent.Sub> { checkAndUpdate() }
    }

    if (skyblockOnly) {
        EventBus.register<AreaEvent.Skyblock> { checkAndUpdate() }
    }

    checkAndUpdate()
    return eventCall
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    return configRegister(configKeys, priority, skyblockOnly, area, subarea, { configValues ->
        configValues.values.all { it as? Boolean == true }
    }, callback)
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    enabledIndices: Set<Int>,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    return configRegister(configKeys, priority, skyblockOnly, area, subarea, { configValues ->
        configValues.values.all { value ->
            when (value) {
                is Int -> value in enabledIndices
                is Boolean -> value
                else -> false
            }
        }
    }, callback)
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    requiredIndex: Int,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    return configRegister(configKeys, priority, skyblockOnly, area, subarea, { configValues ->
        configValues.values.all { value ->
            when (value) {
                is Set<*> -> value.contains(requiredIndex)
                is Boolean -> value
                else -> false
            }
        }
    }, callback)
}