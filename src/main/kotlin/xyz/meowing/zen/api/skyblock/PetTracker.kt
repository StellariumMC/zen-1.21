package xyz.meowing.zen.api.skyblock

import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findThenNull
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object PetTracker {
    private val AUTO_PET = Regex("Autopet equipped your \\[Lvl (?<level>\\d+)] (?<name>.*?)! VIEW RULE")
    private val PET_LEVEL = Regex("Your (?<name>.*?) leveled up to level (?<newLevel>\\d+)!")
    private val PET_ITEM = Regex("Your pet is now holding (?<petItem>.*).")
    private val PET_SUMMON = Regex("You summoned your (?<name>.*?)!")

    private val petData = StoredFile("api/PetTracker")

    var level: Int by petData.int("petLevel")
        private set

    var name: String by petData.string("petName")
        private set

    var item: String by petData.string("petItem")
        private set

    init {
        EventBus.register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val message = event.message.string.removeFormatting()

            AUTO_PET.findThenNull(message, "level", "name") { match ->
                level = match["level"]?.toIntOrNull() ?: 0
                name = match["name"] ?: ""
                item = ""
            } ?: return@register

            PET_LEVEL.findThenNull(message, "newLevel", "name") { match ->
                level = match["newLevel"]?.toIntOrNull() ?: level
                name = match["name"] ?: name
            } ?: return@register

            PET_SUMMON.findThenNull(message, "name") { match ->
                name = match["name"] ?: ""
            } ?: return@register

            PET_ITEM.findThenNull(message, "petItem") { match ->
                item = match["petItem"] ?: ""
            } ?: return@register

            if (message.startsWith("You despawned your")) {
                level = 0
                name = ""
                item = ""
            }
        }
    }
}