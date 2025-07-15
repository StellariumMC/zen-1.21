package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.Render2D.renderString
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext

object slayerstats : Feature("slayerstats") {
    private var kills = 0
    private var sessionStart = System.currentTimeMillis()
    private var totalKillTime = 0L
    private const val name = "SlayerStats"

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerstats",
                "Slayer stats",
                "Shows stats about your kill times",
                ElementType.Switch(false),
                { config -> config["slayertimer"] as? Boolean == true}
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "§c[Zen] §f§lSlayer Stats:\n§7> §bTotal bosses§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s", "Slayers")
        register<GuiEvent.HUD> { renderHUD(it.context) }
    }

    fun addKill(killtime: Long) {
        kills++
        totalKillTime += killtime
    }

    fun reset() {
        kills = 0
        sessionStart = System.currentTimeMillis()
        totalKillTime = 0L
        ChatUtils.addMessage("§c[Zen] §fSlayer stats reset!")
    }
    
    private fun getBPH() = (kills * 3600000 / (System.currentTimeMillis() - sessionStart)).toInt()
    private fun getAVG() = "${(totalKillTime / kills / 1000.0).format(1)}s"

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    private fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled(name) || (kills == 0)) return

        val lines = if (kills > 0) {
            listOf(
                "§c[Zen] §f§lSlayer Stats: ",
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
            val yOffset = index * (mc.textRenderer.fontHeight + linePadding)
            renderString(context, line, x, y + yOffset / scale, scale)
        }
    }
}

object SlayerStatsCommand : CommandUtils(
    "slayerstats",
    listOf("zenslayers")
) {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        ChatUtils.addMessage("§c[Zen] §fPlease use §c/slayerstats reset")
        return 1
    }

    override fun buildCommand(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            ClientCommandManager.argument("action", StringArgumentType.string())
                .executes { context ->
                    val action = StringArgumentType.getString(context, "action")
                    if (action == "reset") slayerstats.reset()
                    else ChatUtils.addMessage("§c[Zen] §fPlease use §c/slayerstats reset")
                    1
                }
        )
    }
}