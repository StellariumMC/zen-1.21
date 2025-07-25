package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.*
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.ScoreboardUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.fromNow
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.text.Text
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object VengTimer : Feature("vengtimer") {
    private var starttime = TimeUtils.zero
    private var hit = false
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private var isFighting = false
    private var cachedNametag: Entity? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Blaze", ConfigElement(
                "vengtimer",
                "Vengeance proc timer",
                "Time until vengeance procs.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register("vengtimer", "§bVeng proc: §c4.3s")

        register<ScoreboardEvent.Update> { event ->
            val sidebarLines = ScoreboardUtils.getSidebarLines(true)

            for (line in sidebarLines) {
                when {
                    line.contains("Slay the boss!") && !isFighting -> isFighting = true
                    line.contains("Boss slain!") && isFighting -> cleanup()
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            if (fail.matcher(event.message.string.removeFormatting()).matches() && isFighting) TickUtils.schedule(10) { cleanup() }
        }

        register<EntityEvent.Attack> { event ->
            if (hit || event.target !is BlazeEntity || !isFighting) return@register

            val player = player ?: return@register
            val heldItem = player.mainHandStack ?: return@register

            if (event.player.name?.string != player.name?.string || !heldItem.name.string.removeFormatting().contains("Pyrochaos Dagger", true)) return@register

            val nametagEntity = cachedNametag ?: world?.entities?.find { entity ->
                val name = entity.name?.string?.removeFormatting() ?: return@find false
                name.contains("Spawned by") && name.endsWith("by: ${player.name?.string}")
            }?.also { cachedNametag = it }

            if (nametagEntity != null && event.target.id == (nametagEntity.id - 3)) {
                starttime = 6.seconds.fromNow
                hit = true
                TickUtils.schedule(119) {
                    starttime = TimeUtils.zero
                    hit = false
                }
            }
        }

        register<GuiEvent.HUD> { renderHUD(it.context) }
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        starttime = TimeUtils.zero
    }

    private fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled("vengtimer")) return

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
}