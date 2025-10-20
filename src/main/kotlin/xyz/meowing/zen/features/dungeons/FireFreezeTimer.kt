package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.sound.SoundEvents
import xyz.meowing.zen.ui.ConfigManager

@Zen.Module
object FireFreezeTimer : Feature("firefreeze", area = "catacombs", subarea = listOf("F3", "M3")) {
    var ticks = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigManager
            .addFeature("Fire Freeze Timer", "", "Dungeons", xyz.meowing.zen.ui.ConfigElement(
                "firefreeze",
                ElementType.Switch(false)
            ))

        return configUI
    }

    override fun initialize() {
        HUDManager.register("firefreeze", "§bFire freeze: §c4.3s")

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                createTimer(105,
                    onTick = {
                        if (ticks > 0) ticks--
                    },
                    onComplete = {
                        Utils.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1f, 0.5f)
                        ticks = 0
                    }
                )
                ticks = 100
            }
        }

        register<RenderEvent.HUD> { renderHUD(it.context) }
    }

    override fun onRegister() {
        ticks = 0
        super.onRegister()
    }

    override fun onUnregister() {
        ticks = 0
        super.onUnregister()
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