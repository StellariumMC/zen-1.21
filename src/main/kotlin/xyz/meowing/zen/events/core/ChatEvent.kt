@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.network.chat.Component
import xyz.meowing.knit.api.events.CancellableEvent

sealed class ChatEvent {
    /**
     * Posted when the player receives a chat or actionbar message.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Receive(
        val message: Component,
        val isActionBar: Boolean
    ) : CancellableEvent()

    /**
     * Posted when the player sends a chat message.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Send(
        val message: String,
        val chatUtils: Boolean
    ) : CancellableEvent()
}