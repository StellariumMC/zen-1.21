package xyz.meowing.zen.features.general.keyShortcuts

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.knit.api.text.core.ClickEvent
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Command

@Command
object KeybindCommand : Commodore("keybind", "zenkeybind", "zenkb") {
    init {
        runs {
            if (!KeyShortcuts.isEnabled()) {
                val message = KnitText
                    .literal("$prefix §cYou do not have the feature §bKeyShortcuts §cenabled!")
                    .onHover("Click to enable feature.")
                    .onClick(ClickEvent.RunCommand("/zen updateConfig keyShortcuts true false"))

                KnitChat.fakeMessage(message)
                return@runs
            }

            TickScheduler.Client.post {
                client.setScreen(KeybindGui())
            }
        }
    }
}