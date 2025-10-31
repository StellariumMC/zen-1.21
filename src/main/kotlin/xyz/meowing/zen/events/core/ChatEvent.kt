@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.text.Text
import xyz.meowing.knit.api.events.CancellableEvent

sealed class ChatEvent {
    class Receive(
        val message: Text,
        val isActionBar: Boolean
    ) : CancellableEvent()

    class Send(
        val message: String,
        val chatUtils: Boolean
    ) : CancellableEvent()
}