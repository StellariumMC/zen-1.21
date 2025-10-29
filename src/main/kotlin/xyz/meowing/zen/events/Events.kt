package xyz.meowing.zen.events

import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.api.ItemAbility
import xyz.meowing.zen.api.PartyTracker.PartyMember
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import xyz.meowing.knit.api.render.world.RenderContext

abstract class Event

abstract class CancellableEvent : Event() {
    private var cancelled = false
    fun cancel() { cancelled = true }
    fun isCancelled() = cancelled
}

abstract class InternalEvent {
    abstract class NeuAPI {
        class Load() : Event()
    }
}

abstract class SkyblockEvent {
    abstract class Slayer {
        class Spawn(val entity: Entity?, val entityID: Int, val packet: EntityTrackerUpdateS2CPacket) : Event()
        class Death(val entity: Entity, val entityID: Int) : Event()
        class Cleanup() : Event()
        class Fail() : Event()
        class QuestStart() : Event()
    }

    class ItemAbilityUsed(val ability: ItemAbility.ItemAbility) : Event()
    class EntitySpawn(val skyblockMob: EntityDetection.SkyblockMob) : Event()
    class DamageSplash(val damage: Int, val originalName: String, val entityPos: Vec3d, val packet: EntityTrackerUpdateS2CPacket, val entity: Entity) : CancellableEvent()
}

abstract class MouseEvent {
    class Click(val button: Int) : CancellableEvent()
    class Release(val button: Int) : Event()
    class Scroll(val horizontal: Double, val vertical: Double) : Event()
    class Move() : Event()
}

abstract class KeyEvent {
    class Press(val keyCode: Int, val scanCode: Int, val modifiers: Int) : CancellableEvent()
    class Release(val keyCode: Int, val scanCode: Int, val modifiers: Int) : CancellableEvent()
}

abstract class TickEvent {
    class Client : Event()
    class Server : Event()
}

abstract class GameEvent {
    class Load : Event()
    class Unload : Event()
    class ActionBar(val message: Text) : CancellableEvent()
    class Disconnect() : Event()
}

abstract class PartyEvent {
    class Changed(val type: PartyChangeType, val playerName: String? = null, val members: Map<String, PartyMember>) : Event()
}

class ItemTooltipEvent(val stack: ItemStack, val context: Item.TooltipContext, val type: TooltipType, val lines: MutableList<Text>) : Event()

enum class PartyChangeType {
    MEMBER_JOINED, MEMBER_LEFT, PLAYER_JOINED, PLAYER_LEFT, LEADER_CHANGED, DISBANDED, LIST, PARTY_FINDER
}

abstract class RenderEvent {
    class World(
        val context: RenderContext
    ) : Event()

    class WorldPostEntities(
        val context: RenderContext
    ) : Event()

    class BlockOutline(
        val context: RenderContext
    ) : CancellableEvent()

    class EntityGlow(val entity: net.minecraft.entity.Entity, var shouldGlow: Boolean, var glowColor: Int) : Event()
    class HUD(val context: DrawContext) : Event()
    class GuardianLaser(val entity: net.minecraft.entity.Entity, val target: net.minecraft.entity.Entity?) : CancellableEvent()

    abstract class Entity {
        class Pre(val entity: net.minecraft.entity.Entity, val matrices: MatrixStack, val vertex: VertexConsumerProvider?, val light: Int) : CancellableEvent()
        class Post(val entity: net.minecraft.entity.Entity, val matrices: MatrixStack, val vertex: VertexConsumerProvider?, val light: Int) : Event()
    }

    abstract class Player {
        class Pre(val entity: PlayerEntityRenderState, val matrices: MatrixStack) : CancellableEvent()
    }
}

abstract class EntityEvent {
    class Join(val entity: Entity) : Event()
    class Leave(val entity: Entity) : Event()
    class Death(val entity: Entity) : Event()
    class Attack(val player: PlayerEntity, val target: Entity) : Event()
    class Metadata(val packet: EntityTrackerUpdateS2CPacket, val entity: Entity, val name: String) : CancellableEvent()
    class Spawn(val packet: EntitySpawnS2CPacket) : CancellableEvent()
    class Interact(val player: PlayerEntity, val world: World, val hand: Hand, val action: String, val pos: BlockPos? = null) : Event()
    class ArrowHit(val shooterName: String, val hitEntity: Entity) : Event()
    class ItemToss(val stack: ItemStack) : CancellableEvent()
}

abstract class GuiEvent {
    class AfterRender(val screen: Screen, val context: DrawContext) : Event()
    @Deprecated("Use RenderEvent.HUD instead.")
    class HUD(val context: DrawContext) : Event()
    class Open(val screen: Screen) : Event()
    class Close(val screen: Screen, val handler: ScreenHandler) : CancellableEvent()
    class Click(val mx: Double, val my: Double, val mbtn: Int, val state: Boolean, val screen: Screen) : CancellableEvent()
    class Key(val keyName: String?, val key: Int, val character: Char, val scanCode: Int, val screen: Screen) : CancellableEvent()

    abstract class Slot {
        class Click(val slot: net.minecraft.screen.slot.Slot?, val slotId: Int, val button: Int, val actionType: SlotActionType, val handler: ScreenHandler, val screen: HandledScreen<*>) : CancellableEvent()
        class Render(val context: DrawContext, val slot: net.minecraft.screen.slot.Slot, val screen: HandledScreen<ScreenHandler>) : Event()
    }
}

abstract class ChatEvent {
    class Receive(val message: Text) : CancellableEvent()
    class Send(val message: String, val chatUtils: Boolean) : CancellableEvent()
}

abstract class WorldEvent {
    class Change(val world: ClientWorld) : Event() {
        companion object {
            private val lastChangeTime = java.util.concurrent.atomic.AtomicLong(0L)
            private const val COOLDOWN_MS = 500L

            fun shouldPost(): Boolean {
                val currentTime = System.currentTimeMillis()
                val lastTime = lastChangeTime.get()

                if (currentTime - lastTime < COOLDOWN_MS) {
                    return false
                }
                return lastChangeTime.compareAndSet(lastTime, currentTime)
            }
        }
    }
}

abstract class PacketEvent {
    class Received(val packet: Packet<*>) : CancellableEvent()
    class Sent(val packet: Packet<*>) : Event()
    class ReceivedPost(val packet: Packet<*>) : Event()
    class SentPost(val packet: Packet<*>) : Event()
}

class SidebarUpdateEvent(val lines: List<String>) : Event()

abstract class TablistEvent {
    class Update(val packet: PlayerListS2CPacket) : Event()
}

abstract class AreaEvent {
    class Main(val area: String) : Event()
    class Sub(val subarea: String) : Event()
    class Skyblock(val newVal: Boolean) : Event()
}