package xyz.meowing.zen.features.hud.tradeHistory

import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Command


@Command
object TradeHistoryCommand : Commodore("tradelogs", "zentl", "zentrades") {
    init {
        runs {
            TickScheduler.Client.schedule(2) {
                client.setScreen(TradeHistoryHUD())
            }
        }
    }
}