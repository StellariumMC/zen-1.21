package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event

sealed class GameEvent {
    class Start : Event()

    class Stop : Event()
}