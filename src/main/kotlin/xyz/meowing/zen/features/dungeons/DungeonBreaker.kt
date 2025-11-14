package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.PacketEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object DungeonBreaker : Feature(
    "dungeonBreaker",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private const val NAME = "Dungeon Breaker Charges"
    private val regex = "Charges: (\\d+)/(\\d+)⸕".toRegex()
    private var charges = 0
    private var max = 0

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Breaker charge display",
                "Shows the amount of breaker charges left.",
                "Dungeons",
                ConfigElement(
                    "dungeonBreaker",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.register(NAME, "§bCharges: §e20§7/§e20§c⸕", "dungeonBreaker")

        register<PacketEvent.ReceivedPost> { event ->
            if (event.packet is ClientboundContainerSetSlotPacket) {
                val stack = event.packet.item ?: return@register
                if (stack.getData(DataTypes.SKYBLOCK_ID)?.skyblockId != "DUNGEONBREAKER") return@register

                stack.lore.firstNotNullOfOrNull { regex.find(it.removeFormatting()) }?.let { match ->
                    charges = match.groupValues[1].toIntOrNull() ?: 0
                    max = match.groupValues[2].toIntOrNull() ?: 0
                }
            }
        }

        register<GuiEvent.Render.HUD> { event ->
            if (max == 0) return@register
            val x = HUDManager.getX(NAME)
            val y = HUDManager.getY(NAME)
            val scale = HUDManager.getScale(NAME)

            Render2D.renderString(event.context, "§bCharges: §e${charges}§7/§e${max}§c⸕", x, y, scale)
        }
    }
}