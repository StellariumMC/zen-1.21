package meowing.zen.features.hud

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.MouseEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ItemUtils.displayName
import meowing.zen.utils.ItemUtils.extraAttributes
import meowing.zen.utils.LoopUtils
import meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext

@Zen.Module
object FatalTempoOverlay : Feature("fataltempooverlay",true) {
    private val hits = mutableListOf<Long>()
    private var level = 0
    private var currentPercent = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("HUD", "Fatal Tempo Overlay", ConfigElement(
                "fataltempooverlay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("Fatal Tempo", "§eFatal Tempo: §a200%")

        register<EntityEvent.Interact> {
            checkFatal()
        }

        register<MouseEvent.Click> { event ->
            if (event.button == 0) checkFatal()
        }

        register<RenderEvent.HUD> {
            if (HUDManager.isEnabled("Fatal Tempo")) render(it.context)
        }
    }

    private fun checkFatal() {
        val item = player?.mainHandStack ?: return
        val extraAttributes = item.extraAttributes ?: return
        val ftLevel = extraAttributes.getCompound("enchantments")
            .map { it.getInt("ultimate_fatal_tempo") }
            .orElse(null).get()

        if (ftLevel <= 0) return

        level = ftLevel * if (item.displayName().contains("Terminator")) 3 else 1
        val currentTime = System.currentTimeMillis()
        hits.add(currentTime)
        hits.removeAll { currentTime - it > 3000 }
        currentPercent = minOf(200, hits.size * level * 10)

        LoopUtils.setTimeout(3100) {
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

        Render2D.renderString(context, text, x, y, scale)
    }
}