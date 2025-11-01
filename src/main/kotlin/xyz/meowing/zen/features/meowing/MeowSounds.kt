package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.sound.SoundEvents
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object MeowSounds : Feature("meowsounds") {
    private val meowRegex = Regex("(?:Guild|Party|Co-op|From|To)? ?>? ?(?:\\[.+?])? ?[a-zA-Z0-9_]+ ?(?:\\[.+?])?: (.+)")

    override fun addConfig() {
        ConfigManager
            .addFeature("Meow Sounds", "Meow Sounds", "Meowing", ConfigElement(
                "meowsounds",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Plays a meow sound when a message containing 'meow' is received in chat.", "", ConfigElement(
                    "",
                    ElementType.TextParagraph("Plays a meow sound when a message containing 'meow' is received in chat.")
            ))
    }


    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val content = event.message.string.removeFormatting().lowercase()
            val match = meowRegex.find(content) ?: return@register
            if (match.groups[1]?.value?.contains("meow", ignoreCase = true) != true) return@register

            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f)
        }
    }
}