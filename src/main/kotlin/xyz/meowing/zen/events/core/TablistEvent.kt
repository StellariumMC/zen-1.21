@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.network.chat.Component
import xyz.meowing.knit.api.events.Event

sealed class TablistEvent {
    /**
     * Posted when a change in the tablist has occurred.
     *
     * @see xyz.meowing.zen.events.compat.SkyblockAPI
     * @since 1.2.0
     */
    class Change(
        val old: List<List<String>>,
        val new: List<List<Component>>,
    ) : Event()
}