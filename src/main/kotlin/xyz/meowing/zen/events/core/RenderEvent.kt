@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event
import xyz.meowing.knit.api.render.world.RenderContext

sealed class RenderEvent {
    /**
     * Posted when the game tries to render the guardian lasers.
     *
     * @see xyz.meowing.zen.mixins.MixinGuardianEntityRenderer
     * @since 1.2.0
     */
    class GuardianLaser(
        val entity: net.minecraft.entity.Entity,
        val target: net.minecraft.entity.Entity?
    ) : CancellableEvent()

    sealed class World {
        /**
         * Posted at the end of world rendering.
         *
         * @see xyz.meowing.knit.mixins.MixinWorldRenderer
         * @since 1.2.0
         */
        class Last(
            val context: RenderContext
        ) : Event()

        /**
         * Posted after the entities have rendered.
         *
         * @see xyz.meowing.knit.mixins.MixinWorldRenderer
         * @since 1.2.0
         */
        class AfterEntities(
            val context: RenderContext
        ) : Event()

        /**
         * Posted when the block outline is being rendered.
         *
         * @see xyz.meowing.knit.mixins.MixinWorldRenderer
         * @since 1.2.0
         */
        class BlockOutline(
            val context: RenderContext
        ) : CancellableEvent()
    }

    sealed class Entity {
        /**
         * Posted before the entity has rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinEntityRenderDispatcher
         * @since 1.2.0
         */
        class Pre(
            val entity: net.minecraft.entity.Entity,
            val matrices: MatrixStack,
            val vertex: VertexConsumerProvider?,
            val light: Int
        ) : CancellableEvent()

        /**
         * Posted after the entity has rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinEntityRenderDispatcher
         * @since 1.2.0
         */
        class Post(
            val entity: net.minecraft.entity.Entity,
            val matrices: MatrixStack,
            val vertex: VertexConsumerProvider?,
            val light: Int
        ) : Event()
    }

    sealed class Player {
        /**
         * Posted before the player entity has rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinPlayerEntityRenderer
         * @since 1.2.0
         */
        class Pre(
            val entity: PlayerEntityRenderState,
            val matrices: MatrixStack
        ) : CancellableEvent()
    }
}