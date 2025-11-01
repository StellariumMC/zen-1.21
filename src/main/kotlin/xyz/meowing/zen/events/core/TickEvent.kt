@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event

sealed class TickEvent {
    /**
     * Posted at the end of a Client tick event.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Client : Event()

    /**
     * Posted at the end of a Server tick event.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Server : Event()
}