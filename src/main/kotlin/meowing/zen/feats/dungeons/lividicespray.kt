package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext

@Zen.Module
object lividicespray : Feature("lividicespray", area = "catacombs", subarea = listOf("F5", "M5")) {
    private var bossticks = 390
    private val tickCall: EventBus.EventCall = EventBus.register<TickEvent.Server> ({
        bossticks--
        if (bossticks < 0) tickCall.unregister()
    }, false)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Livid", ConfigElement(
                "lividicespray",
                "Livid ice spray timer",
                "Shows the time until you can ice spray livid.",
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

        register<WorldEvent.Change> { cleanup() }

        register<GuiEvent.HUD> { event ->
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