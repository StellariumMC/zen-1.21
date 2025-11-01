@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event

sealed class InternalEvent {
    sealed class NeuAPI {
        class Load : Event()
    }
}