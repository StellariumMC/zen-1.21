package xyz.meowing.zen.features.mining

import net.minecraft.sound.SoundEvents
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.TablistEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Render2D.TextStyle
import xyz.meowing.zen.utils.ScoreboardUtils
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils
import kotlin.math.max

@Module
object MiningAbility : Feature(
    "miningAbility",
    skyblockOnly = true
) {
    private const val NAME = "Mining Ability"
    private val showTitle by ConfigDelegate<Boolean>("miningAbility.showTitle")
    private val COOLDOWN_REGEX = Regex("""(\d+(?:\.\d+)?)s""")

    private var hasWidget: Boolean = false
    private var wasOnCooldown: Boolean = false
    private var lastUpdateTime = TimeUtils.zero
    private var abilityName: String = ""
    private var cooldownSeconds: Float = 0f

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Mining ability",
                "Mining ability cooldown tracker",
                "Mining",
                ConfigElement(
                    "miningAbility",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Show title",
                ConfigElement(
                    "miningAbility.showTitle",
                    ElementType.Switch(true)
                )
            )
    }

    override fun initialize() {
        HUDManager.register(NAME, "§9§lPickaxe Ability:\n§fMining Speed Boost: §aAvailable")

        register<TablistEvent.Change> { parseTablist() }

        register<GuiEvent.Render.HUD> { event ->
            if (HUDManager.isEnabled(NAME) && hasWidget) {
                val x = HUDManager.getX(NAME)
                val y = HUDManager.getY(NAME)
                val scale = HUDManager.getScale(NAME)

                getDisplayLines().forEachIndexed { index, line ->
                    Render2D.renderString(event.context, line, x, y + index * 10 * scale, scale, textStyle = TextStyle.DROP_SHADOW)
                }
            }
        }
    }

    private fun parseTablist() {
        val entries = ScoreboardUtils.getTabListEntriesString()

        val abilityIndex = entries.indexOfFirst { it.contains("Ability", ignoreCase = true) }

        if (abilityIndex == -1 || abilityIndex + 1 >= entries.size) {
            hasWidget = false
            reset()
            return
        }

        hasWidget = true
        val abilityLine = entries[abilityIndex + 1]

        if (!abilityLine.contains(":")) return

        val parts = abilityLine.split(":", limit = 2)
        if (parts.size != 2) return

        abilityName = parts[0].trim()
        val status = parts[1].trim()

        if (status.contains("Available", ignoreCase = true)) {
            cooldownSeconds = 0f
            lastUpdateTime = TimeUtils.zero
        } else {
            if (lastUpdateTime == TimeUtils.zero) {
                val match = COOLDOWN_REGEX.find(status)
                cooldownSeconds = match?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                lastUpdateTime = TimeUtils.now
            }
        }
    }

    private fun getDisplayLines(): List<String> {
        if (!hasWidget || abilityName.isEmpty()) {
            return listOf(
                "§9§lPickaxe Ability:",
                "§fMining Speed Boost: §aAvailable"
            )
        }

        val elapsed = if (lastUpdateTime != TimeUtils.zero) (TimeUtils.now - lastUpdateTime).millis / 1000f else 0f
        val remaining = max(0f, cooldownSeconds - elapsed)
        val isAvailable = remaining <= 0f

        if (isAvailable && wasOnCooldown && showTitle) {
            showTitle("§aAbility Ready!", null, 2000)
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 1f, 1f)
            wasOnCooldown = false
        } else if (!isAvailable) {
            wasOnCooldown = true
        }

        val statusText = if (isAvailable) {
            "§a§lAvailable"
        } else {
            val color = when {
                remaining <= 3f -> "§c"
                remaining <= 10f -> "§e"
                else -> "§6"
            }
            val timeText = if (remaining <= 5f) "%.1fs".format(remaining) else "${remaining.toInt()}s"
            "$color$timeText"
        }

        return listOf(
            "§9§lPickaxe Ability:",
            "§f $abilityName: $statusText"
        )
    }

    private fun reset() {
        abilityName = ""
        cooldownSeconds = 0f
        wasOnCooldown = false
        lastUpdateTime = TimeUtils.zero
    }
}