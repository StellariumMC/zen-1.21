package xyz.meowing.zen.features.slayers.carrying

import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.knit.api.text.core.ClickEvent
import xyz.meowing.knit.api.text.core.ColorCodes
import xyz.meowing.zen.annotations.Command
import java.text.SimpleDateFormat
import java.util.*

@Command
object CarryCommand : Commodore("carry", "zencarry") {
    private val carrycounter by ConfigDelegate<Boolean>("carryCounter")
    private var currentLogPage = 1

    init {
        literal("add") {
            executable {
                param("param0") {
                    suggests {
                        buildList {
                            world?.players?.forEach { player ->
                                if (player.name.string.isNotBlank() && player.uuid.version() == 4) {
                                    add(player.name.string)
                                }
                            }
                            CarryCounter.carries.forEach { add(it.name) }
                        }
                    }
                }

                runs { player: String, count: Int ->
                    if (!checkEnabled()) return@runs
                    val carryee = CarryCounter.addCarry(player, count)
                    if (carryee != null) {
                        if (carryee.total == count) {
                            KnitChat.fakeMessage("$prefix §fAdded §b$player§f for §b$count§f carries.")
                        } else {
                            KnitChat.fakeMessage("$prefix §fUpdated §b$player§f to §b${carryee.total}§f total (§b${carryee.count}§f/§b${carryee.total}§f)")
                        }
                    }
                }
            }
        }

        literal("remove") {
            executable {
                param("param0") {
                    suggests { CarryCounter.carries.map { it.name } }
                }

                runs { player: String ->
                    if (!checkEnabled()) return@runs
                    val removed = CarryCounter.removeCarry(player)
                    KnitChat.fakeMessage("$prefix §f${if (removed) "Removed" else "Player not found:"} §b$player")
                }
            }
        }

        literal("settotal") {
            executable {
                param("param0") {
                    suggests { CarryCounter.carries.map { it.name } }
                }

                runs { player: String, total: Int ->
                    if (!checkEnabled()) return@runs
                    val carryee = CarryCounter.findCarry(player)
                    if (carryee != null) {
                        carryee.total = total
                        KnitChat.fakeMessage("$prefix §fSet §b$player§f total to §b$total§f (§b${carryee.count}§f/§b$total§f)")
                    } else {
                        KnitChat.fakeMessage("$prefix §fPlayer §b$player§f not found!")
                    }
                }
            }
        }

        literal("setcount") {
            executable {
                param("param0") {
                    suggests { CarryCounter.carries.map { it.name } }
                }

                runs { player: String, count: Int ->
                    if (!checkEnabled()) return@runs
                    val carryee = CarryCounter.findCarry(player)
                    if (carryee != null) {
                        carryee.count = count
                        KnitChat.fakeMessage("$prefix §fSet §b$player§f count to §b$count§f (§b$count§f/§b${carryee.total}§f)")
                        if (count >= carryee.total) carryee.complete()
                    } else {
                        KnitChat.fakeMessage("$prefix §fPlayer §b$player§f not found!")
                    }
                }
            }
        }

        literal("list") {
            runs {
                if (!checkEnabled()) return@runs
                if (CarryCounter.carries.isEmpty()) {
                    KnitChat.fakeMessage("$prefix §fNo active carries.")
                    return@runs
                }
                KnitChat.fakeMessage("$prefix §fActive Carries:")
                CarryCounter.carries.forEach { carryee ->
                    val progress = "§b${carryee.count}§f/§b${carryee.total}"
                    val lastBoss = if (carryee.count > 0) "§7(${carryee.getTimeSinceLastBoss()} ago)" else ""
                    KnitChat.fakeMessage("§7> §b${carryee.name}§f - $progress $lastBoss")
                }
            }
        }

        literal("clear") {
            runs {
                if (!checkEnabled()) return@runs
                val count = CarryCounter.carries.size
                CarryCounter.clearCarries()
                KnitChat.fakeMessage("$prefix §fCleared §b$count§f carries.")
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
                    client.execute { client.setScreen(HUDEditor()) }
                }
                KnitChat.fakeMessage("$prefix §fOpened HUD editor.")
            }
        }

        runs { showHelp() }
    }

    private fun checkEnabled(): Boolean {
        if (!carrycounter) {
            val message = KnitText
                .literal("$prefix §fPlease enable carry counter first!")
                .onHover("§cClick to open settings GUI")
                .onClick(ClickEvent.RunCommand("/zen"))
                .toVanilla()

            KnitChat.fakeMessage(message)
            return false
        }
        return true
    }

    private fun showLogs(page: Int) {
        if (!checkEnabled()) return

        val logs = CarryCounter.completedCarries.sortedByDescending { it.timestamp }
        if (logs.isEmpty()) {
            KnitChat.fakeMessage("$prefix §fNo carry logs found.")
            return
        }

        val totalCarries = logs.sumOf { it.totalCarries }
        val totalPages = (logs.size + 9) / 10
        currentLogPage = page.coerceIn(1, totalPages)
        val startIndex = (currentLogPage - 1) * 10
        val endIndex = (startIndex + 10).coerceAtMost(logs.size)

        KnitChat.fakeMessage("§7⏤".repeat(40))

        val headerText = KnitText.empty()
            .append(KnitText.fromFormatted("$prefix §fCarry Logs - §fPage §b$currentLogPage§f/§b$totalPages "))
            .append(
                KnitText.literal(if (currentLogPage > 1) "[<]" else "[<]")
                    .color(if (currentLogPage > 1) ColorCodes.AQUA else ColorCodes.GRAY)
                    .apply { if (currentLogPage > 1) runCommand("/carry log ${currentLogPage - 1}") }
            )
            .append(KnitText.fromFormatted(" §7| "))
            .append(
                KnitText.literal(if (currentLogPage < totalPages) "[>]" else "[>]")
                    .color(if (currentLogPage < totalPages) ColorCodes.AQUA else ColorCodes.GRAY)
                    .apply { if (currentLogPage < totalPages) runCommand("/carry log ${currentLogPage + 1}") }
            )

        KnitChat.fakeMessage(headerText.toVanilla())

        logs.subList(startIndex, endIndex).forEach { log ->
            val date = SimpleDateFormat("d/M/yyyy").format(Date(log.timestamp))
            val time = SimpleDateFormat("HH:mm").format(Date(log.timestamp))
            KnitChat.fakeMessage("§7> §b${log.playerName} §7- §c$date §fat §c$time §7- §b${log.totalCarries} §fcarries")
        }

        KnitChat.fakeMessage("§c§l| §fTotal carries: §b$totalCarries")
        KnitChat.fakeMessage("§7⏤".repeat(40))
    }

    private fun showHelp() {
        KnitChat.fakeMessage("$prefix §fCarry Commands:")
        listOf(
            "add §c<player> <count>§7 - §fAdd carries",
            "settotal §c<player> <total>§7 - §fSet total carries",
            "setcount §c<player> <count>§7 - §fSet current count",
            "remove §c<player>§7 - §fRemove player",
            "log §c[page]§7 - §fShow carry history",
            "list§7 - §fShow active carries",
            "clear§7 - §fClear all carries",
            "gui§7 - §fOpen HUD editor"
        ).forEach { KnitChat.fakeMessage("§7> §7/§bcarry $it") }
    }
}