package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.ui.ConfigMenuManager
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object LeapAnnounce : Feature("leapannounce") {
    private val regex = "^You have teleported to (.+)".toRegex()
    private val leapmessage by ConfigDelegate<String>("leapmessage")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Leap announce", "", "Dungeons", xyz.meowing.zen.ui.ConfigElement(
                "leapannounce",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Leap announce message", "Leap announce message", "Options", xyz.meowing.zen.ui.ConfigElement(
                "leapmessage",
                ElementType.TextInput("Leaping to", "Leaping to")
            ))

        return configUI
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val result = regex.find(event.message.string.removeFormatting())
            if (result != null) ChatUtils.command("/pc $leapmessage ${result.groupValues[1]}")
        }
    }
}