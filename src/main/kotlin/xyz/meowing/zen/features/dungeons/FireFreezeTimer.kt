package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
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
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.events.WorldEvent

@Zen.Module
object FireFreezeTimer : Feature("firefreeze", area = "catacombs", subarea = listOf("F3", "M3")) {
    var ticks = 0

    override fun addConfig() {
        ConfigManager
            .addFeature("Fire Freeze Timer", "", "Dungeons", ConfigElement(
                "firefreeze",
                ElementType.Switch(false)
            ))
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

        register<WorldEvent.Change> { ticks = 0 }
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