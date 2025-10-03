package xyz.meowing.zen.config

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.Zen.Companion.openConfig
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.CommandUtils
import xyz.meowing.zen.utils.TickUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Zen.Command
object ConfigCommand : CommandUtils(
    "zen",
    listOf("ma", "meowaddons")
) {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        openConfig()
        return 1
    }

    override fun buildCommand(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            ClientCommandManager.literal("hud")
                .executes { _ ->
                    TickUtils.schedule(1) {
                        mc.execute {
                            mc.setScreen(HUDEditor())
                        }
                    }
                    1
                }
        )
    }
}