package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ChatEvent

@Module
object MeowSounds : Feature(
    "meowSounds",
    "Meow sounds",
    "Plays a meow sound when a message containing 'meow' is received in chat",
    "Meowing"
) {
    private val meowRegex = Regex("(?:Guild|Party|Co-op|From|To)? ?>? ?(?:\\[.+?])? ?[a-zA-Z0-9_]+ ?(?:\\[.+?])?: (.+)")

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val content = event.message.stripped
            val match = meowRegex.find(content) ?: return@register
            if (match.groups[1]?.value?.contains("meow", ignoreCase = true) != true) return@register

            Utils.playSound(SoundEvents.CAT_AMBIENT, 0.8f, 1.0f)
        }
    }
}