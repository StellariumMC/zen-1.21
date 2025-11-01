package xyz.meowing.zen.events.compat

import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.info.ScoreboardTitleUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.info.ScoreboardUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.info.TabListChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.PlayerHotbarChangeEvent
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.PlayerEvent
import xyz.meowing.zen.events.core.ScoreboardEvent
import xyz.meowing.zen.events.core.TablistEvent

/**
 * Handles and converts SkyblockAPI events to our own.
 */
@Module
object SkyblockAPI {
    init {
        SkyBlockAPI.eventBus.register(this)
    }

    @Subscription
    fun onTabListUpdate(event: TabListChangeEvent) {
        EventBus.post(TablistEvent.Change(event.old, event.new))
    }

    @Subscription
    fun onScoreboardTitleUpdate(event: ScoreboardTitleUpdateEvent) {
        EventBus.post(ScoreboardEvent.UpdateTitle(event.old, event.new))
    }

    @Subscription
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        EventBus.post(ScoreboardEvent.Update(event.old, event.new, event.components))
    }

    @Subscription
    fun onPlayerHotbarUpdate(event: PlayerHotbarChangeEvent) {
        EventBus.post(PlayerEvent.HotbarChange(event.slot, event.item))
    }
}