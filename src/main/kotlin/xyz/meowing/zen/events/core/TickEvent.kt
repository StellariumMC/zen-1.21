@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event

sealed class TickEvent {
    class Client : Event()

    class Server : Event()
}