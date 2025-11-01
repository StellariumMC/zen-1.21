package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonFloor
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object LividIceSpray : Feature(
    "lividicespray",
    island = SkyBlockIsland.THE_CATACOMBS,
    dungeonFloor = listOf(DungeonFloor.F5, DungeonFloor.M5)
) {
    private var bossticks = 390
    private val tickCall: EventCall = EventBus.register<TickEvent.Server> (add = false) {
        bossticks--
        if (bossticks < 0) tickCall.unregister()
    }

    override fun addConfig() {
        ConfigManager
            .addFeature("Livid Ice Spray Timer", "", "Dungeons", ConfigElement(
                "lividicespray",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register("Livid ice spray timer", "§bIce spray: §c13.2s")

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.") {
                tickCall.register()
            }
        }

        register<LocationEvent.WorldChange> { cleanup() }

        register<GuiEvent.Render.HUD> { event ->
            if (HUDManager.isEnabled("Livid ice spray timer")) render(event.context)
        }
    }

    private fun cleanup() {
        bossticks = 390
        tickCall.unregister()
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX("Livid ice spray timer")
        val y = HUDManager.getY("Livid ice spray timer")
        val scale = HUDManager.getScale("Livid ice spray timer")
        val time = bossticks / 20
        Render2D.renderStringWithShadow(context, "§bIce spray: §c${time}s", x, y, scale)
    }
}