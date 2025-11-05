package xyz.meowing.zen.features.meowing

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object MeowCount : Feature(
    "meowCount"
) {
    private val meowData = StoredFile("features/MeowCount")
    var meowCount: Int by meowData.int("meowCount", 0)

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Meow count",
                "Counts how many times you have meowed in chat. Use §c/meowcount §rto check your meow count",
                "Meowing",
                ConfigElement(
                    "meowCount",
                    ElementType.Switch(false)
                )
            )
    }


    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (event.message.lowercase().contains("meow")) meowCount++
        }
    }
}

@Command
object MeowCommand : Commodore("meowcount", "zenmeow", "zenmeowcount") {
    init {
        runs {
            val count = MeowCount.meowCount
            KnitChat.fakeMessage("$prefix §fYou have meowed §b$count §ftimes!")
        }
    }
}