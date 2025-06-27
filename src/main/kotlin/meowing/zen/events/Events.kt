package meowing.zen.events

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.text.Text

abstract class Event

abstract class CancellableEvent : Event() {
    private var cancelled = false
    fun cancel() { cancelled = true }
    fun isCancelled() = cancelled
}

class TickEvent : Event()

class RenderWorldEvent(val context: WorldRenderContext?) : Event()

class RenderWorldPostEntitiesEvent(val context: WorldRenderContext?) : Event()

class EntityJoinEvent(val entity: Entity) : Event()

class EntityLeaveEvent(val entity: Entity) : Event()

class ChatReceiveEvent(val message: Text?, val overlay: Boolean) : CancellableEvent()

class WorldChangeEvent(val mc: MinecraftClient?, val world: ClientWorld) : Event()

class GuiAfterRenderEvent(val screen: Screen) : Event()

class GuiOpenEvent(val screen: Screen) : Event()

class GuiCloseEvent(val screen: Screen) : Event()

class GuiClickEvent(val mx: Double, val my: Double, val mbtn: Int, val state: Boolean, val screen: Screen) : CancellableEvent()

class GuiKeyEvent(val keyName: String?, val key: Int, val scanCode: Int, val screen: Screen) : CancellableEvent()

sealed class PacketEvent(val packet: Packet<*>) : Event() {
    class Received(packet: Packet<*>) : PacketEvent(packet)
    class Sent(packet: Packet<*>) : PacketEvent(packet)
}

class EntityMetadataEvent(val packet: EntityTrackerUpdateS2CPacket) : Event()

class ScoreboardEvent(val packet: Packet<*>) : Event()

class TablistEvent(val packet: PlayerListS2CPacket) : Event()

class AreaEvent(val area: String) : Event()

class SubAreaEvent(val subarea: String) : Event()