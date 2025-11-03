package xyz.meowing.zen.features.hud

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.skyblock.PlayerStats
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object SoulflowDisplay : Feature(
    "soulflowDisplay",
    true
) {
    private const val NAME = "Soulflow Display"

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Soulflow display HUD",
                "Display soulflow on HUD",
                "HUD",
                ConfigElement(
                    "soulflowDisplay",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.register(NAME, "§3500⸎ Soulflow")

        register<GuiEvent.Render.HUD> { event ->
            if (HUDManager.isEnabled(NAME)) render(event.context)
        }
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        Render2D.renderStringWithShadow(context,"§3${PlayerStats.soulflow}", x, y, scale)
    }
}