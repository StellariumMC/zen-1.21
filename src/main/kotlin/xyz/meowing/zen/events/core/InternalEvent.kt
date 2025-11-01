@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event

sealed class InternalEvent {
    sealed class NeuAPI {
        /**
         * Posted when the NEUApi has finished loading the item data.
         *
         * @see xyz.meowing.zen.api.NEUApi
         * @since 1.2.0
         */
        class Load : Event()
    }
}