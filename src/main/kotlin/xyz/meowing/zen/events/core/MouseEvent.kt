@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event

sealed class MouseEvent {
    class Click(
        val button: Int
    ) : CancellableEvent()

    class Release(
        val button: Int
    ) : Event()

    class Scroll(
        val horizontal: Double,
        val vertical: Double
    ) : Event()

    class Move : Event()
}