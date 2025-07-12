package meowing.zen.feats.dungeons

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.events.TickEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Render2D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.sound.SoundEvents

object firefreeze : Feature("firefreeze", area = "catacombs", subarea = listOf("F3", "M3")) {
    var ticks = 0
    private var servertickcall: EventBus.EventCall = EventBus.register<TickEvent.Server> ({
        if (ticks > 0) ticks--
    })

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Fire freeze", ConfigElement(
                "firefreeze",
                "Fire freeze timer",
                "Time until you should activate fire freeze",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register("firefreeze", "§bFire freeze: §c4.3s", "Dungeons")

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                ticks = 100
                servertickcall.unregister()
                TickUtils.scheduleServer(105) {
                    Utils.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1f, 0.5f)
                    ticks = 0
                    servertickcall.unregister()
                }
            }
        }

        register<GuiEvent.HUD> { renderHUD(it.context) }
    }

    override fun onRegister() {
        ticks = 0
    }

    override fun onUnregister() {
        ticks = 0
    }

    private fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled("firefreeze") || ticks <= 0) return

        val text = "§bFire freeze: §c${"%.1f".format(ticks / 20.0)}s"
        val x = HUDManager.getX("firefreeze")
        val y = HUDManager.getY("firefreeze")
        val scale = HUDManager.getScale("firefreeze")

        Render2D.renderString(context, text, x, y, scale)
    }
}