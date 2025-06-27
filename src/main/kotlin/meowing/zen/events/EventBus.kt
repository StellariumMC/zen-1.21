package meowing.zen.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.gui.screen.Screen
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.*
import org.lwjgl.glfw.GLFW
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val listeners = ConcurrentHashMap<Class<*>, MutableSet<Any>>()

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            post(TickEvent())
        }
        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            post(EntityJoinEvent(entity))
        }
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            post(EntityLeaveEvent(entity))
        }
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            post(WorldChangeEvent(mc, world))
        }
        ClientReceiveMessageEvents.ALLOW_GAME.register { msg, show ->
            !post(ChatReceiveEvent(msg, show))
        }
        WorldRenderEvents.LAST.register { context ->
            post(RenderWorldEvent(context))
        }
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            post(RenderWorldPostEntitiesEvent(context))
        }
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, mx, my, mbtn ->
                val event = GuiClickEvent(mx, my, mbtn, true, screen)
                post(event)
                !event.isCancelled()
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, mx, my, mbtn ->
                val event = GuiClickEvent(mx, my, mbtn, false, screen)
                post(event)
                !event.isCancelled()
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scancode, _ ->
                val event = GuiKeyEvent(
                    GLFW.glfwGetKeyName(key, scancode),
                    key,
                    scancode,
                    screen
                )
                post(event)
                !event.isCancelled()
            }
            ScreenEvents.remove(screen).register { screen ->
                post(GuiCloseEvent(screen))
            }
            ScreenEvents.afterRender(screen).register { _, context, mouseX, mouseY, tickDelta ->
                post(GuiAfterRenderEvent(screen))
            }
        }
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            if (screen != null) post(GuiOpenEvent(screen))
        }
    }

    fun onPacketReceived(packet: Packet<*>) {
        post(PacketEvent.Received(packet))
        PacketReceived(packet)
    }

    fun onPacketSent(packet: Packet<*>) {
        post(PacketEvent.Sent(packet))
    }

    private fun PacketReceived(packet: Packet<*>) {
        when (packet) {
            is EntityTrackerUpdateS2CPacket -> {
                post(EntityMetadataEvent(packet))
            }
            is ScoreboardObjectiveUpdateS2CPacket, is ScoreboardScoreUpdateS2CPacket, is ScoreboardDisplayS2CPacket, is TeamS2CPacket -> {
                post(ScoreboardEvent(packet))
            }
            is PlayerListS2CPacket -> {
                when (packet.actions.firstOrNull()) {
                    PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME -> {
                        post(TablistEvent(packet))
                    }
                    else -> {}
                }
            }
        }
    }

    inline fun <reified T : Event> register(noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        val handlers = listeners.getOrPut(T::class.java) { ConcurrentHashMap.newKeySet() }
        if (add) handlers.add(callback)
        return EventCallImpl(T::class.java, callback)
    }

    fun <T : Event> post(event: T): Boolean {
        val handlers = listeners[event::class.java] ?: return false
        handlers.forEach { handler ->
            runCatching {
                @Suppress("UNCHECKED_CAST")
                (handler as (T) -> Unit)(event)
            }.onFailure { it.printStackTrace() }
        }
        return when (event) {
            is CancellableEvent -> event.isCancelled()
            else -> false
        }
    }

    class EventCallImpl(
        private val eventClass: Class<*>,
        private val callback: Any
    ) : EventCall {
        override fun unregister(): Boolean = listeners[eventClass]?.remove(callback) ?: false
        override fun register(): Boolean = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }.add(callback)
    }

    interface EventCall {
        fun unregister(): Boolean
        fun register(): Boolean
    }
}