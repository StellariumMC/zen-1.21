@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.util.math.Vec3d
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event
import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.api.ItemAbility

sealed class SkyblockEvent {
    sealed class Slayer {
        /**
         * Posted after the EntityTrackerUpdateS2CPacket for a slayer entity has been received and processed.
         *
         * @see xyz.meowing.zen.api.SlayerTracker
         * @since 1.2.0
         */
        class Spawn(
            val entity: Entity?,
            val entityID: Int,
            val packet: EntityTrackerUpdateS2CPacket
        ) : Event()

        /**
         * Posted after the EntityEvent.Death event for a slayer entity has been received and processed.
         *
         * @see xyz.meowing.zen.api.SlayerTracker
         * @since 1.2.0
         */
        class Death(
            val entity: Entity,
            val entityID: Int
        ) : Event()

        /**
         * Posted to clean up tasks in case of the boss randomly disappearing.
         *
         * @see xyz.meowing.zen.api.SlayerTracker
         * @since 1.2.0
         */
        class Cleanup : Event()

        /**
         * Posted after the current slayer quest has been failed.
         *
         * @see xyz.meowing.zen.api.SlayerTracker
         * @since 1.2.0
         */
        class Fail : Event()

        /**
         * Posted after a new slayer quest has been started.
         *
         * @see xyz.meowing.zen.api.SlayerTracker
         * @since 1.2.0
         */
        class QuestStart : Event()
    }

    /**
     * Posted when an item ability has been used.
     *
     * @see xyz.meowing.zen.api.ItemAbility
     * @since 1.2.0
     */
    class ItemAbilityUsed(
        val ability: ItemAbility.ItemAbility
    ) : Event()

    /**
     * Posted when a new entity with a nametag corresponding to it has been detected.
     *
     * @see xyz.meowing.zen.api.EntityDetection
     * @since 1.2.0
     */
    class EntitySpawn(
        val skyblockMob: EntityDetection.SkyblockMob
    ) : Event()

    /**
     * Posted when the EntityTrackerUpdateS2CPacket for a damage splash has been received and processed.
     *
     * @see xyz.meowing.zen.api.DamageAPI
     * @since 1.2.0
     */
    class DamageSplash(
        val damage: Int,
        val originalName: String,
        val entityPos: Vec3d,
        val packet: EntityTrackerUpdateS2CPacket,
        val entity: Entity
    ) : CancellableEvent()
}
