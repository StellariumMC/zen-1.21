package xyz.meowing.zen.features.dungeons

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.utils.DungeonUtils
import xyz.meowing.zen.utils.LocationUtils
import xyz.meowing.zen.utils.LoopUtils.setTimeout
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object CryptReminder : Feature("cryptreminder") {
    private val cryptreminderdelay by ConfigDelegate<Double>("cryptreminderdelay")

    override fun addConfig() {
        ConfigManager
            .addFeature("Crypt reminder", "Crypt reminder", "Dungeons", ConfigElement(
                "cryptreminder",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Crypt reminder delay", "Crypt reminder delay", "Options", ConfigElement(
                "cryptreminderdelay",
                ElementType.Slider(1.0, 5.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[NPC] Mort: Good luck.") {
                setTimeout(1000 * 60 * cryptreminderdelay.toLong()) {
                    if (DungeonUtils.getCryptCount() == 5 || !LocationUtils.checkArea("catacombs")) return@setTimeout
                    KnitChat.sendCommand("/pc Zen » ${DungeonUtils.getCryptCount()}/5 crypts")
                    showTitle("§c${DungeonUtils.getCryptCount()}§7/§c5 §fcrypts", null, 3000)
                }
            }
        }
    }
}