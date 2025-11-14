package xyz.meowing.zen.features.hud.tradeHistory

import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.utils.TickUtils


@Command
object TradeHistoryCommand : Commodore("tradelogs", "zentl", "zentrades") {
    init {
        runs {
            TickUtils.schedule(2) {
                client.setScreen(TradeHistoryHUD())
            }
        }
    }
}