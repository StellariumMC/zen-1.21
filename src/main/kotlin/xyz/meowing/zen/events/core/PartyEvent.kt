@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event
import xyz.meowing.zen.api.PartyTracker

sealed class PartyEvent {
    /**
     * Posted when a change in the current party has occurred.
     *
     * @see xyz.meowing.zen.api.PartyTracker
     * @since 1.2.0
     */
    class Changed(
        val type: PartyChangeType,
        val playerName: String? = null,
        val members: Map<String, PartyTracker.PartyMember>
    ) : Event()
}

enum class PartyChangeType {
    MEMBER_JOINED,
    MEMBER_LEFT,
    PLAYER_JOINED,
    PLAYER_LEFT,
    LEADER_CHANGED,
    DISBANDED,
    LIST,
    PARTY_FINDER;
}