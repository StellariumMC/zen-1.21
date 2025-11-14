package xyz.meowing.zen.features.general.damageTracker

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Command

@Command
object DamageTrackerCommand : Commodore("damagetracker", "dt", "dmg") {
    init {
        runs {
            TickScheduler.Client.post {
                //client.setScreen(DamageTrackerGui())
                KnitChat.fakeMessage("$prefix§f This feature has been disabled temporarily.")
            }
        }
    }
}