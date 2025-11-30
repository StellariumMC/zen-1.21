package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.utils.ItemUtils.createSkull
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.LocationAPI
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.api.skyblock.EffectsAPI
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.TickEvent

@Module
object BlazeTimers : Feature(
    "blazetimers",
    true,
) {
    private const val NAME = "BlazeTimers"
    private const val POLAR_EFFECT = "Smoldering Polarization I"
    private const val WISP_EFFECT = "Wisp's Ice-Flavored Water I"

    private var ticks = 0
    private var polar = 0
    private var wisp = 0
    private var shouldDisplay = false

    private val polarRegex = "^You ate a Re-heated Gummy Polar Bear!$".toRegex()
    private val wispRegex = Regex("BUFF! You .* with Wisp's Ice-Flavored Water I!")

    private val polarSkull = createSkull("ewogICJ0aW1lc3RhbXAiIDogMTYxNjA2NDQyNDQxMiwKICAicHJvZmlsZUlkIiA6ICI3NDhiMGEwNjFmNDI0ZDZmYjQ5ZjNhMWI3M2RjOWMyZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVfbWFtYW1hIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQzMDY1ODdlYzM4YzI0NDZkMzg5YTU4MWUwNjkxNTU2ZmE1OGZjZTBhMDJkMDg0NmQyM2ZkNjhlMzY1NmEyNDkiCiAgICB9CiAgfQp9")
    private val wispSplash = ItemStack(Items.SPLASH_POTION)

    private data class TimerData(val item: ItemStack, val timeStr: String, val color: String)

    private fun previewTimers(): List<TimerData> {
        return listOf(
            TimerData(polarSkull, "1:00:00", "§a"),
            TimerData(wispSplash, "20:00", "§b")
        )
    }

    override fun addConfig() {
        ConfigManager
            .addFeature(
            "Blaze Timers",
            "Shows Polar Bear and Wisp splash timers",
            "Slayers",
            ConfigElement(
                "blazetimers",
                ElementType.Switch(false))
        )
    }

    override fun initialize() {
        HUDManager.registerCustom(NAME, 80, 40, this::editorRender, "blazetimers")

        polar = EffectsAPI.getTime(POLAR_EFFECT)
        wisp = EffectsAPI.getTime(WISP_EFFECT)

        EffectsAPI.onEffectUpdate { effectName, seconds ->
            when (effectName) {
                POLAR_EFFECT -> polar = seconds
                WISP_EFFECT -> wisp = seconds
            }
        }

        register<LocationEvent.IslandChange> { event ->
            shouldDisplay = event.new == SkyBlockIsland.CRIMSON_ISLE
        }

        shouldDisplay = LocationAPI.island == SkyBlockIsland.CRIMSON_ISLE

        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val text = event.message.string.removeFormatting()
            when {
                text.matches(polarRegex) -> polarUsed()
                text.contains(wispRegex) -> wispUsed()
            }
        }

        register<TickEvent.Client> {
            if (++ticks >= 20) {
                if (polar > 0) {
                    polar--
                    EffectsAPI.updateTime(POLAR_EFFECT, polar)
                }
                if (wisp > 0) {
                    wisp--
                    EffectsAPI.updateTime(WISP_EFFECT, wisp)
                }
                ticks = 0
            }
        }

        register<GuiEvent.Render.HUD> {
            render(it.context)
        }
    }

    private fun polarUsed() {
        EffectsAPI.addTime(POLAR_EFFECT, 3600)
    }

    private fun wispUsed() {
        wisp = 1200
        EffectsAPI.setRemaining(WISP_EFFECT, wisp, save = true)
    }

    private fun getActiveTimers(): List<TimerData> {
        val timers = mutableListOf<TimerData>()
        if (polar > 0) timers.add(TimerData(polarSkull, formatTime(polar), "§a"))
        if (wisp > 0) timers.add(TimerData(wispSplash, formatTime(wisp), "§b"))
        return timers
    }

    private fun render(context: GuiGraphics) {
        if (!shouldDisplay) return

        val activeTimers = getActiveTimers()
        if (activeTimers.isEmpty()) return

        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        drawHUD(context, x, y, scale, activeTimers)
    }

    private fun editorRender(context: GuiGraphics) = drawHUD(context, 0f, 0f, 1f, previewTimers())

    private fun drawHUD(context: GuiGraphics, x: Float, y: Float, scale: Float, timers: List<TimerData>) {
        val iconSize = 16f * scale
        val spacing = 2f * scale
        var currentY = y

        timers.forEach { timer ->
            val textY = currentY + (iconSize - 8f) / 2f
            Render2D.renderItem(context, timer.item, x, currentY, scale)
            Render2D.renderStringWithShadow(context, "${timer.color}${timer.timeStr}", x + iconSize + spacing, textY, scale)
            currentY += iconSize + spacing
        }
    }

    private fun formatTime(seconds: Int): String {
        if (seconds <= 0) return "0:00"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, secs)
        else String.format("%d:%02d", minutes, secs)
    }
}