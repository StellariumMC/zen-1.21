package meowing.zen.feats.slayers

import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.Render2D.renderString
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import kotlin.time.Duration

@Zen.Module
object slayerstats : Feature("slayerstats") {
    private var kills = 0
    private var sessionStart = TimeUtils.now
    private var totalKillTime = Duration.ZERO
    private const val name = "SlayerStats"

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerstats",
                "Slayer stats",
                "Shows stats about your kill times",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "$prefix §f§lSlayer Stats:\n§7> §bTotal bosses§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s")
        register<GuiEvent.HUD> { renderHUD(it.context) }
    }

    fun addKill(killtime: Duration) {
        kills++
        totalKillTime += killtime
    }

    fun reset() {
        kills = 0
        sessionStart = TimeUtils.now
        totalKillTime = Duration.ZERO
        ChatUtils.addMessage("$prefix §fSlayer stats reset!")
    }

    private fun getBPH(): Int {
        val sessionDuration = sessionStart.since
        return if (sessionDuration.millis > 0) (kills * 3600000 / sessionDuration.millis).toInt() else 0
    }
    private fun getAVG() = "${(totalKillTime.millis / kills / 1000.0).format(1)}s"

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    private fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled(name) || (kills == 0)) return

        val lines = if (kills > 0) {
            listOf(
                "$prefix §f§lSlayer Stats: ",
                "§7> §bTotal bosses§f: §c${kills}",
                "§7> §bBosses/hr§f: §c${getBPH()}",
                "§7> §bAvg. kill§f: §c${getAVG()}"
            )
        } else {
            emptyList()
        }

        if (lines.isEmpty()) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        val linePadding = 2
        lines.forEachIndexed { index, line ->
            val yOffset = index * (fontRenderer.fontHeight + linePadding)
            renderString(context, line, x, y + yOffset / scale, scale)
        }
    }
}

@Zen.Command
object SlayerStatsCommand : CommandUtils(
    "slayerstats",
    listOf("zenslayers")
) {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        ChatUtils.addMessage("$prefix §fPlease use §c/slayerstats reset")
        return 1
    }

    override fun buildCommand(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            ClientCommandManager.argument("action", StringArgumentType.string())
                .executes { context ->
                    val action = StringArgumentType.getString(context, "action")
                    if (action == "reset") slayerstats.reset()
                    else ChatUtils.addMessage("$prefix §fPlease use §c/slayerstats reset")
                    1
                }
        )
    }
}