package meowing.zen.feats

import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Debug {
    var debugmode = false
}

@Zen.Command
object DebugCommand : CommandUtils("zendebug") {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val args = context.input.split(" ").drop(1)
        if (args.size == 1 && args[0].lowercase() == "toggle") {
            Debug.debugmode = !Debug.debugmode
            ChatUtils.addMessage("§c[Zen] §fToggled dev mode.")
        }
        return 1
    }
}