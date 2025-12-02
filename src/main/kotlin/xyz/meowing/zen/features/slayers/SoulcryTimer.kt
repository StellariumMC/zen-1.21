package xyz.meowing.zen.features.slayers

import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.fromNow
import xyz.meowing.zen.utils.TimeUtils.millis
import kotlin.time.Duration.Companion.seconds

@Module
object SoulcryTimer : Feature(
    "soulcryTimer",
    true
) {
    private const val NAME = "Soulcry Timer"
    private var startTime = TimeUtils.zero
    private var active = false

    override fun addConfig() {
        ConfigManager.addFeature(
            "Soulcry cooldown",
            "Shows Soulcry ability cooldown",
            "Slayers",
            ConfigElement("soulcryTimer", ElementType.Switch(false))
        )
    }

    override fun initialize() {
        HUDManager.register(NAME, "§c4.0s", "soulcryTimer")

        register<SkyblockEvent.ItemAbilityUsed> { event ->
            if (event.ability.abilityName.contains("Soulcry", ignoreCase = true)) {
                startTime = 4.seconds.fromNow
                active = true
                registerEvent("render")
                TickUtils.schedule(79) {
                    startTime = TimeUtils.zero
                    active = false
                    unregisterEvent("render")
                }
            }
        }

        createCustomEvent<GuiEvent.Render.HUD.Pre>("render") {
            render(it.context)
        }
    }

    private fun render(context: GuiGraphics) {
        val text = getDisplayText()
        if (text.isEmpty()) return

        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        Render2D.renderString(context, text, x, y, scale)
    }

    private fun getDisplayText(): String {
        if (active && startTime.isInFuture) {
            val timeLeft = startTime.until
            val timeLeftInSeconds = timeLeft.millis / 1000.0
            return "Soulcry: §c${"%.1f".format(timeLeftInSeconds)}s"
        }
        return ""
    }
}