package xyz.meowing.zen.features.hud

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import net.minecraft.component.DataComponentTypes
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.events.PacketEvent

@Zen.Module
object SoulflowDisplay : Feature("soulflowdisplay") {
    private const val name = "Soulflow Display"
    private var soulflow = ""

    override fun addConfig() {
        ConfigManager
            .addFeature("Soulflow Display HUD", "", "HUD", ConfigElement(
                "soulflowdisplay",
                ElementType.Switch(false)
            ))
    }


    override fun initialize() {
        HUDManager.register(name, "§3500⸎ Soulflow")

        register<PacketEvent.Received> { event ->
            if (event.packet !is ScreenHandlerSlotUpdateS2CPacket) return@register

            val item = event.packet.stack
            val attributes = item.get(DataComponentTypes.CUSTOM_DATA)?.nbt ?: return@register
            val sbId = attributes.getString("id", "")?.takeIf(String::isNotEmpty) ?: return@register


            if (sbId !in listOf("SOULFLOW_PILE", "SOULFLOW_BATTERY", "SOULFLOW_SUPERCELL")) return@register

            item.get(DataComponentTypes.LORE)?.lines?.map { it.string }?.find {
                it.startsWith("Internalized: ")
            }?.substringAfter("Internalized: ")?.let {
                soulflow = "§3$it"
            }
        }

        register<RenderEvent.HUD> { event ->
            if (HUDManager.isEnabled(name)) render(event.context)
        }
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        Render2D.renderStringWithShadow(context,soulflow, x, y, scale)
    }
}