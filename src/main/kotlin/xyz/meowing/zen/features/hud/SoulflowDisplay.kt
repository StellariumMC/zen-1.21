package xyz.meowing.zen.features.hud

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import xyz.meowing.zen.api.PlayerStats
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager

@Zen.Module
object SoulflowDisplay : Feature("soulflowdisplay") {
    private const val name = "Soulflow Display"

    override fun addConfig() {
        ConfigManager
            .addFeature("Soulflow Display HUD", "", "HUD", ConfigElement(
                "soulflowdisplay",
                ElementType.Switch(false)
            ))
    }


    override fun initialize() {
        HUDManager.register(name, "§3500⸎ Soulflow")

        register<RenderEvent.HUD> { event ->
            if (HUDManager.isEnabled(name)) render(event.context)
        }
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        Render2D.renderStringWithShadow(context,"§3${PlayerStats.soulflow}", x, y, scale)
    }
}