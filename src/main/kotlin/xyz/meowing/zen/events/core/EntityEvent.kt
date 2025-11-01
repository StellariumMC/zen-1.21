@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event

sealed class EntityEvent {
    sealed class Packet {
        class Metadata(
            val packet: EntityTrackerUpdateS2CPacket,
            val entity: Entity,
            val name: String
        ) : CancellableEvent()

        class Spawn(
            val packet: EntitySpawnS2CPacket
        ) : CancellableEvent()
    }

    class Join(
        val entity: Entity
    ) : Event()

    class Leave(
        val entity: Entity
    ) : Event()

    class Death(
        val entity: Entity
    ) : Event()

    class Attack(
        val player: PlayerEntity,
        val target: Entity
    ) : Event()

    class Interact(
        val player: PlayerEntity,
        val world: World,
        val hand: Hand,
        val action: String,
        val pos: BlockPos? = null
    ) : Event()

    class ArrowHit(
        val shooterName: String,
        val hitEntity: Entity
    ) : Event()

    class ItemToss(
        val stack: ItemStack
    ) : CancellableEvent()
}