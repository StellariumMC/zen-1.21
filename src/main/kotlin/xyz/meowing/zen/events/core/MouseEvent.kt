@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event

sealed class MouseEvent {
    /**
     * Posted before the Mouse click is handled by the game.
     *
     * @see xyz.meowing.zen.mixins.MixinMouseHandler
     * @since 1.2.0
     */
    class Click(
        val button: Int
    ) : CancellableEvent()

    /**
     * Posted before the Mouse release is handled by the game.
     *
     * @see xyz.meowing.zen.mixins.MixinMouseHandler
     * @since 1.2.0
     */
    class Release(
        val button: Int
    ) : Event()

    /**
     * Posted before the Mouse scroll is handled by the game.
     *
     * @see xyz.meowing.zen.mixins.MixinMouseHandler
     * @since 1.2.0
     */
    class Scroll(
        val horizontal: Double,
        val vertical: Double
    ) : Event()

    /**
     * Posted before the Mouse move is handled by the game.
     *
     * @see xyz.meowing.zen.mixins.MixinMouseHandler
     * @since 1.2.0
     */
    class Move : Event()
}