@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.knit.api.events.Event

sealed class ScoreboardEvent {
    /**
     * Posted when a change in the scoreboard title has occurred.
     *
     * @see xyz.meowing.zen.events.compat.SkyblockAPI
     * @since 1.2.0
     */
    class UpdateTitle(
        val old: String?,
        val new: String
    ) : Event()

    /**
     * Posted when a change in the scoreboard has occurred.
     *
     * @see xyz.meowing.zen.events.compat.SkyblockAPI
     * @since 1.2.0
     */
    class Update(
        val old: List<String>,
        val new: List<String>,
        val components: List<Component>,
    ) : Event() {
        val added: List<String> = new - old.toSet()
        val removed: List<String> = old - new.toSet()

        private val addedSet: Set<String> = added.toSet()
        private val removedSet: Set<String> = removed.toSet()

        val addedComponents: List<Component> = components.filter { it.stripped in addedSet }
        val removedComponents: List<Component> = components.filter { it.stripped in removedSet }
    }
}