package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.*
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.fromNow
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.BlazeEntity
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.utils.Utils
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object VengTimer : Feature("vengtimer", true) {
    private var starttime = TimeUtils.zero
    private var hit = false
    private var isFighting = false
    private var cachedNametag: Entity? = null

    override fun addConfig() {
        ConfigManager
            .addFeature("Vengeance proc timer", "Vengeance proc timer", "Slayers", ConfigElement(
                "vengtimer",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register("vengtimer", "§bVeng proc: §c4.3s")

        createCustomEvent<RenderEvent.HUD>("render") {
            if (HUDManager.isEnabled("VengTimer")) render(it.context)
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
            if (hit || event.target !is BlazeEntity || !isFighting) return@register

            val player = player ?: return@register
            val heldItem = player.mainHandStack ?: return@register
            val playerName = Utils.currentPlayerName

            if (event.player.name?.string !=  playerName || !heldItem.name.string.removeFormatting().contains("Pyrochaos Dagger", true)) return@register

            val nametagEntity = cachedNametag ?: world?.entities?.find { entity ->
                val name = entity.name?.string?.removeFormatting() ?: return@find false
                name.contains("Spawned by") && name.endsWith("by: ${playerName}")
            }?.also { cachedNametag = it }

            if (nametagEntity != null && event.target.id == (nametagEntity.id - 3)) {
                starttime = 6.seconds.fromNow
                hit = true
                registerEvent("render")
                TickUtils.schedule(119) {
                    starttime = TimeUtils.zero
                    hit = false
                    unregisterEvent("render")
                }
            }
        }
    }

    private fun render(context: DrawContext) {
        val text = getDisplayText()
        if (text.isEmpty()) return

        val x = HUDManager.getX("vengtimer")
        val y = HUDManager.getY("vengtimer")
        val scale = HUDManager.getScale("vengtimer")

        Render2D.renderString(context, text, x, y, scale)
    }

    private fun getDisplayText(): String {
        if (hit && starttime.isInFuture) {
            val timeLeft = starttime.until
            return "§bVeng proc: §c${"%.1f".format(timeLeft)}s"
        }
        return ""
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        starttime = TimeUtils.zero
        unregisterEvent("render")
    }
}