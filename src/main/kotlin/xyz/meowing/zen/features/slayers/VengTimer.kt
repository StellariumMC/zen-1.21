package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.fromNow
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.monster.Blaze
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.utils.TimeUtils.millis
import kotlin.time.Duration.Companion.seconds

@Module
object VengTimer : Feature(
    "vengTimer",
    true
) {
    private const val NAME = "Vengeance Timer"
    private var startTime = TimeUtils.zero
    private var hit = false
    private var isFighting = false
    private var cachedNametag: Entity? = null

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Vengeance proc timer",
                "Vengeance proc timer",
                "Slayers",
                ConfigElement(
                    "vengTimer",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.register(NAME, "§bVeng proc: §c4.3s", "vengTimer")

        createCustomEvent<GuiEvent.Render.HUD.Pre>("render") {
            render(it.context)
        }

        register<SkyblockEvent.Slayer.QuestStart> {
            isFighting = true
        }

        register<SkyblockEvent.Slayer.Death> {
            cleanup()
        }

        register<SkyblockEvent.Slayer.Fail> {
            TickUtils.scheduleServer(10) {
                cleanup()
            }
        }

        register<EntityEvent.Attack> { event ->
            if (hit || event.target !is Blaze || !isFighting) return@register

            val player = player ?: return@register
            val heldItem = player.mainHandItem ?: return@register

            if (event.player.name?.string != player.name?.string || !heldItem.hoverName.string.removeFormatting().contains("Pyrochaos Dagger", true)) return@register

            val nametagEntity = cachedNametag ?: world?.entitiesForRendering()?.find { entity ->
                val name = entity.name?.string?.removeFormatting() ?: return@find false
                name.contains("Spawned by") && name.endsWith("by: ${player.name?.string}")
            }?.also { cachedNametag = it }

            if (nametagEntity != null && event.target.id == (nametagEntity.id - 3)) {
                startTime = 6.seconds.fromNow
                hit = true
                registerEvent("render")
                TickUtils.schedule(119) {
                    startTime = TimeUtils.zero
                    hit = false
                    unregisterEvent("render")
                }
            }
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
        if (hit && startTime.isInFuture) {
            val timeLeft = startTime.until
            val timeLeftInSeconds = timeLeft.millis / 1000.0
            return "§bVeng proc: §c${"%.1f".format(timeLeftInSeconds)}s"
        }
        return ""
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        startTime = TimeUtils.zero
        unregisterEvent("render")
    }
}