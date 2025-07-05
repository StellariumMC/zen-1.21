package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.feats.Feature
import meowing.zen.hud.HudElement
import meowing.zen.hud.HudManager
import meowing.zen.hud.HudRenderer
import meowing.zen.utils.ChatUtils
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Colors

object slayerstats : Feature("slayerstats") {
    var kills = 0
    private var sessionStart = System.currentTimeMillis()
    private var totalKillTime = 0L
    private var hudElement: HudElement? = null

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
        hudElement = HudElement(10f, 150f, 200f, 50f, 1.0f, true, "slayerstats", "Slayer Stats")
        HudManager.registerCustom(hudElement!!, SlayerStatsRenderer(hudElement!!))
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
}

class SlayerStatsRenderer(element: HudElement) : HudRenderer(element) {
    private val linePadding = 2
    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        if (slayerstats.kills == 0 && !HudManager.editMode) return

        val lines =
            if (HudManager.editMode)
                listOf(
                    "§c[Zen] §f§lSlayer Stats: ",
                    "§7> §bTotal bosses§f: §c15",
                    "§7> §bBosses/hr§f: §c12",
                    "§7> §bAvg. kill§f: §c45.2s"
                )
            else
                listOf(
                    "§c[Zen] §f§lSlayer Stats: ",
                    "§7> §bTotal bosses§f: §c${slayerstats.kills}",
                    "§7> §bBosses/hr§f: §c${slayerstats.getBPH()}",
                    "§7> §bAvg. kill§f: §c${slayerstats.getAVG()}"
                )

        val actualX = element.getActualX(mc.window.scaledWidth)
        val actualY = element.getActualY(mc.window.scaledHeight)

        context.matrices.push()
        context.matrices.translate(actualX.toDouble(), actualY.toDouble(), 0.0)
        context.matrices.scale(element.scale, element.scale, 1.0f)

        lines.forEachIndexed { index, line ->
            val yOffset = index * (mc.textRenderer.fontHeight + linePadding)
            context.drawText(mc.textRenderer, line, 0, yOffset, Colors.WHITE, false)
        }

        context.matrices.pop()
    }

    override fun getPreviewSize(): Pair<Float, Float> {
        val lines = listOf(
            "§c[Zen] §f§lSlayer Stats: ",
            "§7> §bTotal bosses§f: §c15",
            "§7> §bBosses/hr§f: §c12",
            "§7> §bAvg. kill§f: §c45.2s"
        )
        val maxWidth = lines.maxOfOrNull { mc.textRenderer.getWidth(it) } ?: 0
        val height = lines.size * (mc.textRenderer.fontHeight + linePadding) - linePadding
        return Pair(maxWidth * element.scale, height * element.scale)
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