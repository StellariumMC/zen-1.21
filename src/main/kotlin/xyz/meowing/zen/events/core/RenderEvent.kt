@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event
import xyz.meowing.knit.api.render.world.RenderContext

//#if MC >= 1.21.9
//$$ import net.minecraft.client.renderer.entity.state.AvatarRenderState
//#else
import net.minecraft.client.renderer.entity.state.PlayerRenderState
//#endif

sealed class RenderEvent {
    /**
     * Posted when the game tries to render the guardian lasers.
     *
     * @see xyz.meowing.zen.mixins.MixinGuardianRenderer
     * @since 1.2.0
     */
    class GuardianLaser(
        val entity: net.minecraft.world.entity.Entity,
        val target: net.minecraft.world.entity.Entity?
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
            val entity: net.minecraft.world.entity.Entity,
            val matrices: PoseStack,
            val vertex: MultiBufferSource?,
            val light: Int
        ) : CancellableEvent()

        /**
         * Posted after the entity has rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinEntityRenderDispatcher
         * @since 1.2.0
         */
        class Post(
            val entity: net.minecraft.world.entity.Entity,
            val matrices: PoseStack,
            val vertex: MultiBufferSource?,
            val light: Int
        ) : Event()
    }

    sealed class Player {
        /**
         * Posted before the player entity has rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinPlayerRenderer
         * @since 1.2.0
         */
        class Pre(
            //#if MC >= 1.21.9
            //$$ val entity: AvatarRenderState,
            //#else
            val entity: PlayerRenderState,
            //#endif
            val matrices: PoseStack
        ) : CancellableEvent()
    }
}