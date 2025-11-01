@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.CancellableEvent

sealed class KeyEvent {
    /**
     * Posted before the Key Press is handled by the game.
     *
     * @see xyz.meowing.zen.mixins.MixinKeyboard
     * @since 1.2.0
     */
    class Press(
        val keyCode: Int,
        val scanCode: Int,
        val modifiers: Int
    ) : CancellableEvent()

    /**
     * Posted before the Key Release is handled by the game.
     *
     * @see xyz.meowing.zen.mixins.MixinKeyboard
     * @since 1.2.0
     */
    class Release(
        val keyCode: Int,
        val scanCode: Int,
        val modifiers: Int
    ) : CancellableEvent()
}