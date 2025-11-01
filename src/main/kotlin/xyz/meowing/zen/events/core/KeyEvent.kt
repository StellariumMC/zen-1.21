@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.CancellableEvent

sealed class KeyEvent {
    class Press(
        val keyCode: Int,
        val scanCode: Int,
        val modifiers: Int
    ) : CancellableEvent()

    class Release(
        val keyCode: Int,
        val scanCode: Int,
        val modifiers: Int
    ) : CancellableEvent()
}