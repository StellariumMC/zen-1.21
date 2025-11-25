package xyz.meowing.zen.features.dungeons

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object LeapAnnounce : Feature(
    "leapAnnounce"
) {
    private val regex = "^You have teleported to (.+)".toRegex()
    private val leapMessage by ConfigDelegate<String>("leapAnnounce.message")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Leap announce",
                "Announces in party chat when you use a leap",
                "Dungeons",
                ConfigElement(
                    "leapAnnounce",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Leap announce message",
                ConfigElement(
                    "leapAnnounce.message",
                    ElementType.TextInput("Leaping to", "Leaping to")
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val result = regex.find(event.message.string.removeFormatting())
            if (result != null) KnitChat.sendCommand("pc $leapMessage ${result.groupValues[1]}")
        }
    }
}