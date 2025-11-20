package xyz.meowing.zen.features.general.chatToTitle

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.knit.api.text.core.ClickEvent
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.utils.TickUtils

@Command
object ChatToTitleCommand : Commodore("title", "zentitle") {
    init {
        runs {
            if (!ChatToTitle.isEnabled()) {
                val message = KnitText
                    .literal("$prefix §cYou do not have the feature §bChat to title §cenabled!")
                    .onHover("Click to enable feature.")
                    .onClick(ClickEvent.RunCommand("/zen updateConfig chatToTitle true false"))
                    .toVanilla()

                KnitChat.fakeMessage(message)
                return@runs
            }

            TickUtils.schedule(2) {
                client.setScreen(ChatToTitleGui())
            }
        }
    }
}