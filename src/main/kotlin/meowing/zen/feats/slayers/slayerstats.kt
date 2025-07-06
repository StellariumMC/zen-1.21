package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDEditor
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors

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
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "§c[Zen] §f§lSlayer Stats:\n§7> §bTotal bosses§f: §c15\n§7> §bBosses/hr§f: §c12\n§7> §bAvg. kill§f: §c45.2s", "Slayers")
        register<GuiEvent.Hud> { renderHUD(it.context) }
    }

    fun addKill(killtime: Long) {
        kills++
        totalKillTime += killtime
    }

    fun getBPH() = (kills * 3600000 / (System.currentTimeMillis() - sessionStart)).toInt()
    fun getAVG() = "${(totalKillTime / kills / 1000.0).format(1)}s"

    fun reset() {
        kills = 0
        sessionStart = System.currentTimeMillis()
        totalKillTime = 0L
        ChatUtils.addMessage("§c[Zen] §fSlayer stats reset!")
    }

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    fun renderHUD(context: DrawContext) {
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

        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1.0f)

        val linePadding = 2
        lines.forEachIndexed { index, line ->
            val yOffset = index * (mc.textRenderer.fontHeight + linePadding)
            context.drawText(mc.textRenderer, line, 0, yOffset.toInt(), Colors.WHITE, false)
        }

        context.matrices.pop()
    }
}

object slayerstatscommand {
    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(
            ClientCommandManager.literal("slayerstats")
                .then(
                    ClientCommandManager.argument("action", StringArgumentType.string())
                        .executes { context ->
                            val action = StringArgumentType.getString(context, "action")
                            if (action == "reset") slayerstats.reset()
                            else ChatUtils.addMessage("§c[Zen] §fPlease use §c/slayerstats reset")
                            1
                        }
                )
                .executes {
                    ChatUtils.addMessage("§c[Zen] §fPlease use §c/slayerstats reset")
                    1
                }
        )

        dispatcher.register(
            ClientCommandManager.literal("zenslayers")
                .then(
                    ClientCommandManager.argument("action", StringArgumentType.string())
                        .executes { context ->
                            val action = StringArgumentType.getString(context, "action")
                            if (action == "reset") slayerstats.reset()
                            else ChatUtils.addMessage("§c[Zen] §fPlease use §c/slayerstats reset")
                            1
                        }
                )
        )
    }
}