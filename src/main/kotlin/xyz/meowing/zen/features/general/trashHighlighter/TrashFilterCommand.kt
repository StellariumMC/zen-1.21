package xyz.meowing.zen.features.general.trashHighlighter

import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Command

@Command
object ProtectItemCommand : Commodore("trashfilter", "zentf") {
    init {
        runs {
            TickScheduler.Client.post {
                client.setScreen(TrashFilterGui())
            }
        }
    }
}