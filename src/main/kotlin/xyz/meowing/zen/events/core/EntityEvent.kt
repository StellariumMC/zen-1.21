@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event

sealed class EntityEvent {
    sealed class Packet {
        /**
         * Posted when the client receives the EntityTrackerUpdateS2CPacket packet.
         *
         * @see xyz.meowing.zen.mixins.MixinClientPacketListener
         * @since 1.2.0
         */
        class Metadata(
            val packet: ClientboundSetEntityDataPacket,
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
            val packet: ClientboundAddEntityPacket
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
     * @see xyz.meowing.zen.mixins.MixinMultiPlayerGameMode
     * @since 1.2.0
     */
    class Attack(
        val player: Player,
        val target: Entity
    ) : Event()

    /**
     * Posted when the player interacts with other blocks or entities.
     *
     * @see xyz.meowing.zen.events.EventBus
     * @since 1.2.0
     */
    class Interact(
        val player: Player,
        val world: Level,
        val hand: InteractionHand,
        val action: String,
        val pos: BlockPos? = null
    ) : Event()

    /**
     * Posted when an entity is hit by a projectile.
     *
     * @see xyz.meowing.zen.mixins.MixinAbstractArrow
     * @since 1.2.0
     */
    class ArrowHit(
        val shooterName: String,
        val hitEntity: Entity
    ) : Event()

    /**
     * Posted when the player entity tosses out items through their inventory.
     *
     * @see xyz.meowing.zen.mixins.MixinLocalPlayer
     * @since 1.2.0
     */
    class ItemToss(
        val stack: ItemStack
    ) : CancellableEvent()
}