package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object SecretsBreakdown : Feature("secretsbreakdown", area = "catacombs") {
    val completeRegex = Regex("""^\s*(Master Mode)?\s?(?:The)? Catacombs - (Entrance|Floor .{1,3})$""")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Secrets Breakdown", ConfigElement(
                "secretsbreakdown",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        EventBus.register<ChatEvent.Receive> { event ->
            completeRegex.find(event.message.string.removeFormatting()) ?: return@register

            TickUtils.schedule(20 * 3) {
                ChatUtils.addMessage("$prefix §fSecret counts:")
                DungeonUtils.getPlayers().forEach { (player, info) ->
                    val secrets = info.intSecrets - info.secrets
                    ChatUtils.addMessage("§7| §b$player §7➜ §b$secrets")
                }
            }
        }
    }

    // Hi KIWI!!!
}