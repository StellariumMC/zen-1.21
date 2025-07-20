package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.*
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.text.Text
import java.util.regex.Pattern

@Zen.Module
object VengTimer : Feature("vengtimer") {
    var starttime: Long = 0
    var hit = false
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private var isFighting = false
    private var cachedNametag: net.minecraft.entity.Entity? = null

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
            val world = mc.world ?: return@register
            val scoreboard = world.scoreboard

            val scores = scoreboard.knownScoreHolders
            for (scoreHolder in scores) {
                val playerName = scoreHolder.nameForScoreboard
                if (playerName.startsWith("#")) continue

                val team = scoreboard.getScoreHolderTeam(playerName)
                val displayName = team?.decorateName(Text.literal(playerName))?.string ?: playerName
                val cleanName = displayName.removeFormatting()

                when {
                    cleanName.contains("Slay the boss!") && !isFighting -> isFighting = true
                    cleanName.contains("Boss slain!") && isFighting -> cleanup()
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            if (fail.matcher(event.message.string.removeFormatting()).matches() && isFighting) TickUtils.schedule(10) { cleanup() }
        }

        register<EntityEvent.Attack> { event ->
            if (hit || event.target !is BlazeEntity || !isFighting) return@register

            val player = mc.player ?: return@register
            val heldItem = player.mainHandStack ?: return@register

            if (event.player.name?.string != player.name?.string ||
                !heldItem.name.string.removeFormatting().contains("Pyrochaos Dagger", true)) return@register

            val nametagEntity = cachedNametag ?: mc.world?.entities?.find { entity ->
                val name = entity.name?.string?.removeFormatting() ?: return@find false
                name.contains("Spawned by") && name.endsWith("by: ${player.name?.string}")
            }?.also { cachedNametag = it }

            if (nametagEntity != null && event.target.id == (nametagEntity.id - 3)) {
                starttime = System.currentTimeMillis() + 6000
                hit = true
                TickUtils.schedule(119) {
                    starttime = 0
                    hit = false
                }
            }
        }

        register<GuiEvent.HUD> { renderHUD(it.context) }
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        if (starttime > 0) starttime = 0
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
        if (hit && starttime > 0) {
            val timeLeft = (starttime - System.currentTimeMillis()) / 1000.0
            if (timeLeft > 0) return "§bVeng proc: §c${"%.1f".format(timeLeft)}s"
        }
        return ""
    }
}