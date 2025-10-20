package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager

@Zen.Module
object DungeonBreaker : Feature("dungeonbreaker", area = "catacombs") {
    private const val name = "Dungeon Breaker Charges"
    private val regex = "Charges: (\\d+)/(\\d+)⸕".toRegex()
    private var charges = 0
    private var max = 0

    override fun addConfig() {
        ConfigManager
            .addFeature("Breaker Charge Display", "", "Dungeons", ConfigElement(
                "dungeonbreaker",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "§bCharges: §e20§7/§e20§c⸕")

        register<PacketEvent.ReceivedPost> { event ->
            if (event.packet is ScreenHandlerSlotUpdateS2CPacket) {
                val stack = event.packet.stack ?: return@register
                if (stack.skyblockID != "DUNGEONBREAKER") return@register

                stack.lore.firstNotNullOfOrNull { regex.find(it.removeFormatting()) }?.let { match ->
                    charges = match.groupValues[1].toIntOrNull() ?: 0
                    max = match.groupValues[2].toIntOrNull() ?: 0
                }
            }
        }

        register<RenderEvent.HUD> { event ->
            if (max == 0 || !HUDManager.isEnabled(name)) return@register
            val x = HUDManager.getX(name)
            val y = HUDManager.getY(name)
            val scale = HUDManager.getScale(name)

            Render2D.renderString(event.context, "§bCharges: §e${charges}§7/§e${max}§c⸕", x, y, scale)
        }
    }
}