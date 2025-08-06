package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.api.PlayerStats
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.GameEvent
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.features.dungeons.FireFreezeTimer.ticks
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.removeFormatting
import net.minecraft.client.gui.DrawContext

@Zen.Module
object RoomSecrets: Feature("showRoomSecrets", "catacombs") {
    val regex = Regex("""\b([0-9]|10)/([0-9]|10)\s+Secrets\b""")
    var text = ""

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Room Secrets Hud", ConfigElement(
                "showRoomSecrets",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("secretshud", "§fSecrets: §a7§7/§a7")

        register<GameEvent.ActionBar> { event ->
            val match = regex.find(event.message.string.removeFormatting())
            if (match == null) {
                text = "§fSecrets: §7None"
                return@register
            }

            val (foundStr, totalStr) = match.destructured

            val found = foundStr.toInt()
            val total = totalStr.toInt()

            val percent = found.toFloat() / total.toFloat()

            text = when {
                percent < 0.5f -> "§fSecrets: §c$found§7/§c$total"
                percent < 1f   -> "§fSecrets: §e$found§7/§e$total"
                else           -> "§fSecrets: §a$found§7/§a$total"
            }
        }

        register<GuiEvent.HUD> { renderHUD(it.context) }
    }

    private fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled("secretshud")) return

        val x = HUDManager.getX("secretshud")
        val y = HUDManager.getY("secretshud")
        val scale = HUDManager.getScale("secretshud")

        Render2D.renderString(context, text, x, y, scale)
        // Eclipse was here :3
    }
}