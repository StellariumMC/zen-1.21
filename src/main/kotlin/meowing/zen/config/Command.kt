package meowing.zen.config

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.openConfig
import meowing.zen.hud.HUDEditor
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

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