package xyz.meowing.zen.features.slayers

import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.TitleUtils

@Module
object MinibossSpawn : Feature(
    "minibossSpawn",
    "Miniboss spawn alert",
    "Miniboss spawn alert for slayers",
    "Slayers",
    skyblockOnly = true
) {
    private val showTitle by config.switch("Show title", true)
    private val titleText by config.textInput("Title text", "§fMiniboss Spawned!")

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val message = event.message.stripped

            if (message.contains("SLAYER MINI-BOSS") && message.contains("has spawned!")) {
                val minibossName = extractMinibossName(message) ?: return@register

                Utils.playSound(SoundEvents.CAT_AMBIENT, 1f, 1f)
                KnitChat.fakeMessage("$prefix §b$minibossName §fspawned.")

                if (showTitle) {
                    TitleUtils.showTitle("§b$minibossName", titleText, 2000, scale = 3.0f)
                }
            }
        }
    }

    private fun extractMinibossName(message: String): String? {
        val start = message.indexOf("MINI-BOSS", ignoreCase = true).takeIf { it != -1 } ?: return null
        val end = message.indexOf(" has", start, ignoreCase = true).takeIf { it != -1 } ?: message.length
        return message.substring(start + 10, end).trim().ifEmpty { null }
    }
}