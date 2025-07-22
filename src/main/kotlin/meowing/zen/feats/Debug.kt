package meowing.zen.feats

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.api.PlayerStats
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.LocationUtils.inSkyblock
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Debug {
    var debugmode = false
}

@Zen.Command
object DebugCommand : CommandUtils("zendebug") {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        ChatUtils.addMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats>")
        return 1
    }

    override fun buildCommand(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            ClientCommandManager.argument("action", StringArgumentType.string())
                .executes { context ->
                    val action = StringArgumentType.getString(context, "action")
                    when (action.lowercase()) {
                        "toggle" -> {
                            Debug.debugmode = !Debug.debugmode
                            ChatUtils.addMessage("$prefix §fToggled dev mode.")
                        }
                        "stats" -> {
                            ChatUtils.addMessage(
                                "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.maxHealth} | §6Absorb: ${PlayerStats.absorption}\n" +
                                        "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                                        "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                                        "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}"
                            )
                        }
                        "dgutils" -> {
                            ChatUtils.addMessage(
                                "Crypt Count: ${DungeonUtils.getCryptCount()}\n" +
                                        "Current Class: ${DungeonUtils.getCurrentClass()} ${DungeonUtils.getCurrentLevel()}\n" +
                                        "isMage: ${DungeonUtils.isMage()}"
                            )
                        }
                        "info" -> {
                            ChatUtils.addMessage("inSkyblock: $inSkyblock")
                        }
                        else -> {
                            ChatUtils.addMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats|dgutils|info>")
                        }
                    }
                    1
                }
        )
    }
}