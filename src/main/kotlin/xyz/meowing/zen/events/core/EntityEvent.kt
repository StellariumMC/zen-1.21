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
        /**
         * Posted when the client receives the EntityTrackerUpdateS2CPacket packet.
         *
         * @see xyz.meowing.zen.mixins.MixinClientPlayNetworkHandler
         * @since 1.2.0
         */
        class Metadata(
            val packet: EntityTrackerUpdateS2CPacket,
            val entity: Entity,
            val name: String
        ) : CancellableEvent()

        /**
         * Posted when the client receives the EntitySpawnS2CPacket packet.
         *
         * @see xyz.meowing.knit.api.events.EventBus
         * @since 1.2.0
         */
        class Spawn(
            val packet: EntitySpawnS2CPacket
        ) : CancellableEvent()
    }

    /**
     * Posted when the entity spawns into the ClientWorld
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Join(
        val entity: Entity
    ) : Event()

    /**
     * Posted when the entity either dies or is removed through any other way.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Leave(
        val entity: Entity
    ) : Event()

    /**
     * Posted when the entity dies.
     *
     * @see xyz.meowing.zen.mixins.MixinLivingEntity
     * @since 1.2.0
     */
    class Death(
        val entity: Entity
    ) : Event()

    /**
     * Posted when the entity is attacked by the player entity.
     *
     * @see xyz.meowing.zen.mixins.MixinClientPlayerInteraction
     * @since 1.2.0
     */
    class Attack(
        val player: PlayerEntity,
        val target: Entity
    ) : Event()

    /**
     * Posted when the player interacts with other blocks or entities.
     *
     * @see xyz.meowing.zen.events.EventBus
     * @since 1.2.0
     */
    class Interact(
        val player: PlayerEntity,
        val world: World,
        val hand: Hand,
        val action: String,
        val pos: BlockPos? = null
    ) : Event()

    /**
     * Posted when an entity is hit by a projectile.
     *
     * @see xyz.meowing.zen.mixins.MixinPersistentProjectileEntity
     * @since 1.2.0
     */
    class ArrowHit(
        val shooterName: String,
        val hitEntity: Entity
    ) : Event()

    /**
     * Posted when the player entity tosses out items through their inventory.
     *
     * @see xyz.meowing.zen.mixins.MixinClientPlayerEntity
     * @since 1.2.0
     */
    class ItemToss(
        val stack: ItemStack
    ) : CancellableEvent()
}