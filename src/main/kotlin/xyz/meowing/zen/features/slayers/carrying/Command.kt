package xyz.meowing.zen.features.slayers.carrying

import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TickUtils
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import java.text.SimpleDateFormat
import java.util.*

@Zen.Command
object CarryCommand : Commodore("carry", "zencarry") {
    private val carrycounter by ConfigDelegate<Boolean>("carrycounter")
    private var currentLogPage = 1

    init {
        literal("add") {
            executable {
                param("player") {
                    suggests {
                        buildList {
                            mc.world?.players?.forEach { player ->
                                if (player.name.string.isNotBlank() && player.uuid.version() == 4) {
                                    add(player.name.string)
                                }
                            }
                            CarryCounter.carryees.forEach { add(it.name) }
                        }
                    }
                }
                runs { player: String, count: Int ->
                    if (!checkEnabled()) return@runs
                    val carryee = CarryCounter.addCarryee(player, count)
                    if (carryee != null) {
                        if (carryee.total == count) {
                            ChatUtils.addMessage("$prefix §fAdded §b$player§f for §b$count§f carries.")
                        } else {
                            ChatUtils.addMessage("$prefix §fUpdated §b$player§f to §b${carryee.total}§f total (§b${carryee.count}§f/§b${carryee.total}§f)")
                        }
                    }
                }
            }
        }

        literal("remove") {
            executable {
                param("player") {
                    suggests { CarryCounter.carryees.map { it.name } }
                }
                runs { player: String ->
                    if (!checkEnabled()) return@runs
                    val removed = CarryCounter.removeCarryee(player)
                    ChatUtils.addMessage("$prefix §f${if (removed) "Removed" else "Player not found:"} §b$player")
                }
            }
        }

        literal("settotal") {
            executable {
                param("player") {
                    suggests { CarryCounter.carryees.map { it.name } }
                }
                runs { player: String, total: Int ->
                    if (!checkEnabled()) return@runs
                    val carryee = CarryCounter.findCarryee(player)
                    if (carryee != null) {
                        carryee.total = total
                        ChatUtils.addMessage("$prefix §fSet §b$player§f total to §b$total§f (§b${carryee.count}§f/§b$total§f)")
                    } else {
                        ChatUtils.addMessage("$prefix §fPlayer §b$player§f not found!")
                    }
                }
            }
        }

        literal("setcount") {
            executable {
                param("player") {
                    suggests { CarryCounter.carryees.map { it.name } }
                }
                runs { player: String, count: Int ->
                    if (!checkEnabled()) return@runs
                    val carryee = CarryCounter.findCarryee(player)
                    if (carryee != null) {
                        carryee.count = count
                        ChatUtils.addMessage("$prefix §fSet §b$player§f count to §b$count§f (§b$count§f/§b${carryee.total}§f)")
                        if (count >= carryee.total) carryee.complete()
                    } else {
                        ChatUtils.addMessage("$prefix §fPlayer §b$player§f not found!")
                    }
                }
            }
        }

        literal("list") {
            runs {
                if (!checkEnabled()) return@runs
                if (CarryCounter.carryees.isEmpty()) {
                    ChatUtils.addMessage("$prefix §fNo active carries.")
                    return@runs
                }
                ChatUtils.addMessage("$prefix §fActive Carries:")
                CarryCounter.carryees.forEach { carryee ->
                    val progress = "§b${carryee.count}§f/§b${carryee.total}"
                    val lastBoss = if (carryee.count > 0) "§7(${carryee.getTimeSinceLastBoss()} ago)" else ""
                    ChatUtils.addMessage("§7> §b${carryee.name}§f - $progress $lastBoss")
                }
            }
        }

        literal("clear") {
            runs {
                if (!checkEnabled()) return@runs
                val count = CarryCounter.carryees.size
                CarryCounter.clearCarryees()
                ChatUtils.addMessage("$prefix §fCleared §b$count§f carries.")
            }
        }

        literal("log", "logs") {
            runs { showLogs(currentLogPage) }
            executable {
                runs { page: Int -> showLogs(page) }
            }
        }

        literal("gui") {
            runs {
                if (!checkEnabled()) return@runs
                TickUtils.schedule(2) {
                    mc.execute { mc.setScreen(HUDEditor()) }
                }
                ChatUtils.addMessage("$prefix §fOpened HUD editor.")
            }
        }

        runs { showHelp() }
    }

    private fun checkEnabled(): Boolean {
        if (!carrycounter) {
            ChatUtils.addMessage(
                "$prefix §fPlease enable carry counter first!",
                "§cClick to open settings GUI",
                ClickEvent.Action.RUN_COMMAND,
                "/zen"
            )
            return false
        }
        return true
    }

    private fun showLogs(page: Int) {
        if (!checkEnabled()) return

        val logs = CarryCounter.dataUtils.getData().completedCarries.sortedByDescending { it.timestamp }
        if (logs.isEmpty()) {
            ChatUtils.addMessage("$prefix §fNo carry logs found.")
            return
        }

        val totalCarries = logs.sumOf { it.totalCarries }
        val totalPages = (logs.size + 9) / 10
        currentLogPage = page.coerceIn(1, totalPages)

        val startIndex = (currentLogPage - 1) * 10
        val endIndex = (startIndex + 10).coerceAtMost(logs.size)

        ChatUtils.addMessage("§7⏤".repeat(40))

        val prevPage = if (currentLogPage > 1) "§b[<]" else "§7[<]"
        val nextPage = if (currentLogPage < totalPages) "§b[>]" else "§7[>]"

        val headerText = Text.literal("$prefix §fCarry Logs - §fPage §b$currentLogPage§f/§b$totalPages ")
            .append(Text.literal(prevPage).styled {
                if (currentLogPage > 1) it.withClickEvent(ClickEvent.RunCommand("/carry log ${currentLogPage - 1}"))
                else it
            })
            .append(Text.literal(" §7| "))
            .append(Text.literal(nextPage).styled {
                if (currentLogPage < totalPages) it.withClickEvent(ClickEvent.RunCommand("/carry log ${currentLogPage + 1}"))
                else it
            })

        mc.inGameHud.chatHud.addMessage(headerText)

        logs.subList(startIndex, endIndex).forEach { log ->
            val date = SimpleDateFormat("d/M/yyyy").format(Date(log.timestamp))
            val time = SimpleDateFormat("HH:mm").format(Date(log.timestamp))
            ChatUtils.addMessage("§7> §b${log.playerName} §7- §c$date §fat §c$time §7- §b${log.totalCarries} §fcarries")
        }

        ChatUtils.addMessage("§c§l| §fTotal carries: §b$totalCarries")
        ChatUtils.addMessage("§7⏤".repeat(40))
    }

    private fun showHelp() {
        ChatUtils.addMessage("$prefix §fCarry Commands:")
        listOf(
            "add §c<player> <count>§7 - §fAdd carries",
            "settotal §c<player> <total>§7 - §fSet total carries",
            "setcount §c<player> <count>§7 - §fSet current count",
            "remove §c<player>§7 - §fRemove player",
            "log §c[page]§7 - §fShow carry history",
            "list§7 - §fShow active carries",
            "clear§7 - §fClear all carries",
            "gui§7 - §fOpen HUD editor"
        ).forEach { ChatUtils.addMessage("§7> §7/§bcarry $it") }
    }
}