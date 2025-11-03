package xyz.meowing.zen.features.hud

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.displayName
import xyz.meowing.zen.utils.ItemUtils.extraAttributes
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.knit.api.scheduler.TimeScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.MouseEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object FatalTempoOverlay : Feature(
    "fatalTempoOverlay",
    true
) {
    private val hits = mutableListOf<Long>()
    private var level = 0
    private var currentPercent = 0

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Fatal tempo overlay",
                "Display fatal tempo stacks",
                "HUD",
                ConfigElement(
                    "fatalTempoOverlay",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.register("Fatal Tempo", "§eFatal Tempo: §a200%")

        register<EntityEvent.Interact> {
            checkFatal()
        }

        register<MouseEvent.Click> { event ->
            if (event.button == 0) checkFatal()
        }

        register<GuiEvent.Render.HUD> {
            if (HUDManager.isEnabled("Fatal Tempo")) render(it.context)
        }
    }

    private fun checkFatal() {
        val item = KnitPlayer.heldItem ?: return
        val extraAttributes = item.extraAttributes ?: return
        val ftLevel = extraAttributes.getCompound("enchantments")
            .map { it.getInt("ultimate_fatal_tempo") }
            .orElse(null)?.get() ?: return

        if (ftLevel <= 0) return

        level = ftLevel * if (item.displayName().contains("Terminator")) 3 else 1
        val currentTime = System.currentTimeMillis()
        hits.add(currentTime)
        hits.removeAll { currentTime - it > 3000 }
        currentPercent = minOf(200, hits.size * level * 10)

        TimeScheduler.schedule(3100) {
            hits.removeAll { System.currentTimeMillis() - it > 3000 }
            currentPercent = minOf(200, hits.size * level * 10)
        }
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX("Fatal Tempo")
        val y = HUDManager.getY("Fatal Tempo")
        val scale = HUDManager.getScale("Fatal Tempo")

        val color = if (currentPercent > 0) "§a" else "§c"
        val text = "§eFatal Tempo: $color$currentPercent%"

        Render2D.renderString(context, text, x, y, scale, textStyle = Render2D.TextStyle.DROP_SHADOW)
    }
}