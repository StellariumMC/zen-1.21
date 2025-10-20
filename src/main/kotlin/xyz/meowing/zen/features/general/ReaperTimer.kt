package xyz.meowing.zen.features.general

import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.TickEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render2D

@Zen.Module
object ReaperTimer : Feature("reapertimer", true) {
    private const val name = "Reaper Timer"
    private val indices = listOf(36, 37, 38)
    private var reaped = false
    private var ticks = 120

    override fun addConfig() {
        ConfigManager
            .addFeature("Reaper Timer", "", "General", ConfigElement(
                "reapertimer",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "§c4.2s")

        createCustomEvent<RenderEvent.HUD>("render") { event ->
            val x = HUDManager.getX(name)
            val y = HUDManager.getY(name)
            val scale = HUDManager.getScale(name)
            val time = ticks / 20.0
            Render2D.renderStringWithShadow(event.context, "§c${"%.1f".format(time)}s", x, y, scale)
        }

        register<TickEvent.Server> {
            if (reaped || player?.isSneaking == false) return@register

            if (indices.all { player!!.inventory.getStack(it).skyblockID.contains("REAPER", true) }) {
                reaped = true

                createTimer(120,
                    onTick = {
                        if (ticks > 0) ticks--
                    },
                    onComplete = {
                        ticks = 120
                        unregisterEvent("render")
                    }
                )

                createTimer(490,
                    onComplete = {
                        reaped = false
                    }
                )

                registerEvent("render")
            }
        }
    }
}