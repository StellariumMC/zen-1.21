package meowing.zen.events

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.text.Text

abstract class Event

abstract class CancellableEvent : Event() {
    private var cancelled = false
    fun cancel() { cancelled = true }
    fun isCancelled() = cancelled
}

abstract class TickEvent {
    class Client : Event()
    class Server : Event()
}

abstract class GameEvent {
    class Load : Event()
    class Unload : Event()
}

abstract class RenderEvent {
    class World(val context: WorldRenderContext?) : Event()
    class WorldPostEntities(val context: WorldRenderContext?) : Event()
    class EntityPre(val entity: Entity, val matrices: MatrixStack, val vertex: VertexConsumerProvider, val light: Int) : CancellableEvent()
    class BlockOutline(val worldContext: WorldRenderContext, val blockContext: WorldRenderContext.BlockOutlineContext) : CancellableEvent()
    class Hud(val context: DrawContext) : Event()
}

abstract class EntityEvent {
    class Join(val entity: Entity) : Event()
    class Leave(val entity: Entity) : Event()
    class Attack(val player: PlayerEntity, val target: Entity) : Event()
    class Metadata(val packet: EntityTrackerUpdateS2CPacket) : Event()
}

abstract class GuiEvent {
    class AfterRender(val screen: Screen) : Event()
    class Open(val screen: Screen) : Event()
    class Close(val screen: Screen) : Event()
    class Click(val mx: Double, val my: Double, val mbtn: Int, val state: Boolean, val screen: Screen) : CancellableEvent()
    class Key(val keyName: String?, val key: Int, val scanCode: Int, val screen: Screen) : CancellableEvent()
}

abstract class ChatEvent {
    class Receive(val message: Text?, val overlay: Boolean) : CancellableEvent()
}

abstract class WorldEvent {
    class Change(val mc: MinecraftClient?, val world: ClientWorld) : Event()
}

abstract class PacketEvent {
    class Received(val packet: Packet<*>) : Event()
    class Sent(val packet: Packet<*>) : Event()
}

abstract class ScoreboardEvent {
    class Update(val packet: Packet<*>) : Event()
}

abstract class TablistEvent {
    class Update(val packet: PlayerListS2CPacket) : Event()
}

abstract class AreaEvent {
    class Main(val area: String) : Event()
    class Sub(val subarea: String) : Event()
}