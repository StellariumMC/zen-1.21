package xyz.meowing.zen.features.dungeons

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.scheduler.TimeScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object CryptReminder : Feature(
    "cryptReminder",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private val cryptReminderDelay by ConfigDelegate<Double>("cryptReminder.delay")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Crypt reminder",
                "Reminds you to complete crypts in dungeons",
                "Dungeons",
                ConfigElement(
                    "cryptReminder",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Crypt reminder delay",
                ConfigElement(
                    "cryptReminder.delay",
                    ElementType.Slider(1.0, 5.0, 2.0, false)
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[NPC] Mort: Good luck.") {
                TimeScheduler.schedule(1000 * 60 * cryptReminderDelay.toLong()) {
                    if (DungeonAPI.cryptCount == 5 || !SkyBlockIsland.THE_CATACOMBS.inIsland() || DungeonAPI.inBoss) return@schedule
                    KnitChat.sendCommand("pc Zen » ${DungeonAPI.cryptCount}/5 crypts")
                    showTitle("§c${DungeonAPI.cryptCount}§7/§c5 §fcrypts", null, 3000)
                }
            }
        }
    }
}