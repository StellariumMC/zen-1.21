@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import xyz.meowing.knit.api.events.Event
import xyz.meowing.zen.api.PartyTracker

sealed class PartyEvent {
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