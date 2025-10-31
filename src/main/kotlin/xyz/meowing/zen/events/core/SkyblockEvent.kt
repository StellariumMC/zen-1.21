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
        class Spawn(
            val entity: Entity?,
            val entityID: Int,
            val packet: EntityTrackerUpdateS2CPacket
        ) : Event()

        class Death(
            val entity: Entity,
            val entityID: Int
        ) : Event()

        class Cleanup : Event()

        class Fail : Event()

        class QuestStart : Event()
    }

    class ItemAbilityUsed(
        val ability: ItemAbility.ItemAbility
    ) : Event()

    class EntitySpawn(
        val skyblockMob: EntityDetection.SkyblockMob
    ) : Event()

    class DamageSplash(
        val damage: Int,
        val originalName: String,
        val entityPos: Vec3d,
        val packet: EntityTrackerUpdateS2CPacket,
        val entity: Entity
    ) : CancellableEvent()
}
