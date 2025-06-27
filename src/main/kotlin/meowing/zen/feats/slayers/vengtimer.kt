package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import meowing.zen.feats.Feature
import meowing.zen.hud.HudElement
import meowing.zen.hud.HudManager
import meowing.zen.hud.HudRenderer
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.regex.Pattern

object vengtimer : Feature("vengtimer") {
    var starttime: Long = 0
    var hit = false
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private var isFighting = false
    private var cachedNametag: net.minecraft.entity.Entity? = null
    private var hudElement: HudElement? = null

    override fun initialize() {
        hudElement = HudElement(100f, 200f, 100f, 20f, 1.0f, true, "vengtimer", "Veng Timer")
        HudManager.registerCustom(hudElement!!, VengTimerRenderer(hudElement!!))

        register<ScoreboardEvent> { event ->
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

        register<ChatReceiveEvent> { event ->
            if (fail.matcher(event.message!!.string.removeFormatting()).matches() && isFighting) TickUtils.schedule(10) { cleanup() }
        }

        register<AttackEntityEvent> { event ->
            if (hit || event.target !is BlazeEntity || !isFighting) return@register

            val player = mc.player ?: return@register
            val heldItem = player.mainHandStack ?: return@register

            if (event.entityPlayer.name?.string != player.name?.string ||
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
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        if (starttime > 0) starttime = 0
    }

    fun getDisplayText(): String {
        if (hit && starttime > 0) {
            val timeLeft = (starttime - System.currentTimeMillis()) / 1000.0
            if (timeLeft > 0) return "§bVeng proc: §c${"%.1f".format(timeLeft)}s"
        }
        return ""
    }
}

class VengTimerRenderer(element: HudElement) : HudRenderer(element) {
    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        val text = vengtimer.getDisplayText()
        if (text.isEmpty() && !HudManager.editMode) return

        val displayText =
            if (HudManager.editMode && text.isEmpty()) "§bVeng proc: §c4.3s"
            else text

        val actualX = element.getActualX(mc.window.scaledWidth)
        val actualY = element.getActualY(mc.window.scaledHeight)

        context.matrices.push()
        context.matrices.translate(actualX.toDouble(), actualY.toDouble(), 0.0)
        context.matrices.scale(element.scale, element.scale, 1.0f)
        context.drawText(mc.textRenderer, displayText, 0, 0, Colors.WHITE, false)
        context.matrices.pop()
    }

    override fun getPreviewSize(): Pair<Float, Float> {
        val text = if (HudManager.editMode) "§bVeng proc: §c4.3s" else vengtimer.getDisplayText()
        val displayText = text.ifEmpty { "§bVeng proc: §c4.3s" }
        return Pair(
            mc.textRenderer.getWidth(displayText) * element.scale,
            mc.textRenderer.fontHeight * element.scale
        )
    }
}