package xyz.meowing.zen.features.meowing

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager

data class Data(var meowcount: Double = 0.0)

@Zen.Module
object meowcount : Feature("meowcount") {
    private val dataUtils = DataUtils("meowcount", Data())

    override fun addConfig() {
        ConfigManager
            .addFeature("Meow count", "Meow count", "Meowing", ConfigElement(
                "meowcount",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Counts how many times you have meowed in chat. You can use the command §c/meowcount §rto check your meow count.", "", ConfigElement(
                    "",
                    ElementType.TextParagraph("Counts how many times you have meowed in chat. You can use the command §c/meowcount §rto check your meow count.")
            ))
    }


    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (event.message.lowercase().contains("meow")) {
                dataUtils.updateAndSave {
                    meowcount++
                }
            }
        }
    }

    fun getMeowCount(): Double = dataUtils.getData().meowcount
}

@Zen.Command
object MeowCommand : Commodore("meowcount", "zenmeow", "zenmeowcount") {
    init {
        runs {
            val count = meowcount.getMeowCount().toInt()
            KnitChat.fakeMessage("$prefix §fYou have meowed §b$count §ftimes!")
        }
    }
}