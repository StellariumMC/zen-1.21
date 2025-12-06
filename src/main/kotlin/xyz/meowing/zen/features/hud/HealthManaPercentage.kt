package xyz.meowing.zen.features.hud

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.skyblock.PlayerStats
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.GuiEvent

@Module
object HealthManaPercentage : Feature(
    "healthManaPercent",
    "Health/Mana Display",
    "Show health/mana percentages and hide vanilla HUD elements",
    "HUD",
    skyblockOnly = true
) {
    private const val HEALTH_NAME = "Health Percentage"
    private const val MANA_NAME = "Mana Percentage"

    private val patterns = listOf(
        Regex("""(§.)(?<currentHealth>[\d,]+)/(?<maxHealth>[\d,]+)❤"""),
        Regex("""§b(?<currentMana>[\d,]+)/(?<maxMana>[\d,]+)✎( Mana)?"""),
        Regex("""§a(?<defense>[\d,]+)§a❈ Defense""")
    )

    private val showHealth by config.switch("Show health", true)
    private val showManaPercent by config.switch("Show mana", true)
    private val showAbsoluteValues by config.switch("show absolute values")

    private val hideVanillaHearts by config.switch("Hide vanilla hearts")
    private val hideVanillaArmor by config.switch("Hide vanilla armor")
    private val hideExpBar by config.switch("Hide exp bar")
    private val hideActionBarStats by config.switch("Hide action bar stats")

    @JvmStatic
    fun shouldHideVanillaHearts(): Boolean = isEnabled() && hideVanillaHearts

    @JvmStatic
    fun shouldHideVanillaArmor(): Boolean = isEnabled() && hideVanillaArmor

    @JvmStatic
    fun shouldHideExpBar(): Boolean = isEnabled() && hideExpBar

    override fun initialize() {
        HUDManager.registerCustom(HEALTH_NAME, 20, 10, this::healthEditorRender, "healthmanapercent.showHealth")
        HUDManager.registerCustom(MANA_NAME, 20, 10, this::manaEditorRender, "healthmanapercent.showMana")

        register<ChatEvent.Receive> { event ->
            if (hideActionBarStats && event.isActionBar) {
                cleanActionBarStats(event)
            }
        }

        register<GuiEvent.Render.HUD.Pre> { event ->
            render(event.context)
        }
    }

    private fun healthEditorRender(context: GuiGraphics) {
        val text = if (showAbsoluteValues) "850/1000 (§e85%§c)" else "§c85%"
        Render2D.renderStringWithShadow(context, text, 0f, 0f, 1f)
    }

    private fun manaEditorRender(context: GuiGraphics) {
        val text = if (showAbsoluteValues) "210/500 (§e42%§b)" else "§b42%"
        Render2D.renderStringWithShadow(context, text, 0f, 0f, 1f)
    }

    private fun cleanActionBarStats(event: ChatEvent.Receive) {
        if (isEnabled() && hideActionBarStats && event.isActionBar) {
            val text = event.message.string

            var cleanedText = text
            patterns.forEach { pattern ->
                cleanedText = pattern.replace(cleanedText, "")
            }

            cleanedText = cleanedText.trim().replace("§r  ", " ").replace("\\s+".toRegex(), " ")

            if (cleanedText.isEmpty()) {
                event.cancel()
            } else if (cleanedText != text) {
                event.cancel()
                player?.displayClientMessage(Component.literal(cleanedText), true)
            }
        }
    }

    private fun render(context: GuiGraphics) {
        if (showHealth) renderHealthPercent(context)
        if (showManaPercent) renderManaPercent(context)
    }

    private fun renderHealthPercent(context: GuiGraphics) {
        val currentHealth = PlayerStats.health
        val maxHealth = PlayerStats.maxHealth
        if (maxHealth == 0) return

        val isOverMax = currentHealth > maxHealth
        val percent = (currentHealth.toDouble() / maxHealth * 100).toInt()

        val color = if (isOverMax) "§6" else "§c"
        val text = if (showAbsoluteValues) {
            "$color$currentHealth/$maxHealth"
        } else {
            "$color${percent}%"
        }

        val x = HUDManager.getX(HEALTH_NAME)
        val y = HUDManager.getY(HEALTH_NAME)
        val scale = HUDManager.getScale(HEALTH_NAME)

        Render2D.renderStringWithShadow(context, text, x, y, scale)
    }

    private fun renderManaPercent(context: GuiGraphics) {
        val mana = PlayerStats.mana
        val maxMana = PlayerStats.maxMana
        if (maxMana == 0) return

        val percent = (mana.toDouble() / maxMana * 100).toInt()
        val text = if (showAbsoluteValues) {
            "§b$mana/$maxMana"
        } else {
            "§b${percent}%"
        }

        val x = HUDManager.getX(MANA_NAME)
        val y = HUDManager.getY(MANA_NAME)
        val scale = HUDManager.getScale(MANA_NAME)

        Render2D.renderStringWithShadow(context, text, x, y, scale)
    }
}