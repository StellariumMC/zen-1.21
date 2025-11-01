@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event
import xyz.meowing.knit.api.render.world.RenderContext

sealed class RenderEvent {
    class EntityGlow(
        val entity: net.minecraft.entity.Entity,
        var shouldGlow: Boolean,
        var glowColor: Int
    ) : Event()

    class GuardianLaser(
        val entity: net.minecraft.entity.Entity,
        val target: net.minecraft.entity.Entity?
    ) : CancellableEvent()

    sealed class World {
        class Last(
            val context: RenderContext
        ) : Event()

        class AfterEntities(
            val context: RenderContext
        ) : Event()

        class BlockOutline(
            val context: RenderContext
        ) : CancellableEvent()
    }

    sealed class Entity {
        class Pre(
            val entity: net.minecraft.entity.Entity,
            val matrices: MatrixStack,
            val vertex: VertexConsumerProvider?,
            val light: Int
        ) : CancellableEvent()

        class Post(
            val entity: net.minecraft.entity.Entity,
            val matrices: MatrixStack,
            val vertex: VertexConsumerProvider?,
            val light: Int
        ) : Event()
    }

    sealed class Player {
        class Pre(
            val entity: PlayerEntityRenderState,
            val matrices: MatrixStack
        ) : CancellableEvent()
    }
}