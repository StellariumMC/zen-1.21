package meowing.zen.utils

import net.minecraft.text.Text
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraft.client.MinecraftClient
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import java.util.Objects
import java.util.Optional

object EventTypes {
    open class BaseEvent
    class GameMessageEvent(
        val message: Text,
        val overlay: Boolean
    ) {
        @Volatile
        var hide: Boolean = false
            private set

        init {
            Objects.requireNonNull(message, "Message cannot be null")
        }

        fun hide() {
            this.hide = true
        }

        fun unhide() {
            this.hide = false
        }

        fun isHidden(): Boolean {
            return hide
        }

        fun getPlainText(): String {
            return message.string
        }
    }

    open class EntityEvent protected constructor(
        val entity: Entity,
        val world: World
    ) {
        init {
            Objects.requireNonNull(entity, "Entity cannot be null")
            Objects.requireNonNull(world, "World cannot be null")
        }

        fun isPlayer(): Boolean {
            return entity is net.minecraft.entity.player.PlayerEntity
        }

        fun isLiving(): Boolean {
            return entity is net.minecraft.entity.LivingEntity
        }

        fun getEntityId(): Int {
            return entity.id
        }
    }

    class EntityUnloadEvent(entity: Entity, world: World) : EntityEvent(entity, world)

    class EntityLoadEvent(entity: Entity, world: World) : EntityEvent(entity, world)

    class ClientTickEvent(val client: MinecraftClient) {
        init {
            Objects.requireNonNull(client, "Client cannot be null")
        }

        fun isInGame(): Boolean {
            return client.world != null && client.player != null
        }

        fun isPaused(): Boolean {
            return client.isPaused
        }
    }

    class WorldRenderEvent(val context: WorldRenderContext) {
        init {
            Objects.requireNonNull(context, "Context cannot be null")
        }

        fun getTickDelta(): Float {
            return context.tickCounter().getTickProgress(false)
        }
    }

    class EntityTrackerUpdateEvent(private val packet: net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket) : BaseEvent() {
        fun getEntityId(): Int = packet.id()
        fun getCustomName(): String? =
            packet.trackedValues()?.firstOrNull { it.id == 2 }?.value.let { value ->
                when (value) {
                    is Text -> value.string
                    is Optional<*> -> (value.orElse(null) as Text).string
                    else -> null
                }
            }
    }
}