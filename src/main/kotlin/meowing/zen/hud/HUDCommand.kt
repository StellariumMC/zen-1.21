package meowing.zen.hud

import com.mojang.brigadier.CommandDispatcher
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.TickUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object HUDCommand {
    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(
            ClientCommandManager.literal("zenhud")
                .executes { context ->
                    TickUtils.schedule(2) {
                        mc.execute {
                            mc.setScreen(HUDEditor())
                        }
                    }
                    1
                }
        )
    }
}