package meowing.zen.features.slayers.carrying

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.hud.HUDEditor
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import java.text.SimpleDateFormat
import java.util.*

@Zen.Command
object CarryCommand : CommandUtils("carry", listOf("zencarry")) {
    private val carrycounter by ConfigDelegate<Boolean>("carrycounter")
    private var currentLogPage = 1

    private val playerSuggestions = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        mc.world?.players?.forEach { player ->
            if (player.name.string.isNotBlank() && player.uuid.version() == 4) builder.suggest(player.name.string)
        }
        CarryCounter.carryees.forEach { builder.suggest(it.name) }
        builder.buildFuture()
    }

    private val carryeeSuggestions = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        CarryCounter.carryees.forEach { builder.suggest(it.name) }
        builder.buildFuture()
    }

    override fun buildCommand(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .then(literal("add")
                .then(argument("player", StringArgumentType.word())
                    .suggests(playerSuggestions)
                    .then(argument("count", IntegerArgumentType.integer(1))
                        .executes { addCarryee(it) })))
            .then(literal("remove")
                .then(argument("player", StringArgumentType.word())
                    .suggests(carryeeSuggestions)
                    .executes { removeCarryee(it) }))
            .then(literal("settotal")
                .then(argument("player", StringArgumentType.word())
                    .suggests(carryeeSuggestions)
                    .then(argument("total", IntegerArgumentType.integer(1))
                        .executes { setTotal(it) })))
            .then(literal("setcount")
                .then(argument("player", StringArgumentType.word())
                    .suggests(carryeeSuggestions)
                    .then(argument("count", IntegerArgumentType.integer(0))
                        .executes { setCount(it) })))
            .then(literal("list").executes { listCarryees(it) })
            .then(literal("clear").executes { clearCarryees(it) })
            .then(literal("log")
                .executes { showLogs(it, currentLogPage) }
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes { showLogs(it, IntegerArgumentType.getInteger(it, "page")) }))
            .then(literal("logs")
                .executes { showLogs(it, currentLogPage) }
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes { showLogs(it, IntegerArgumentType.getInteger(it, "page")) }))
            .then(literal("gui").executes { openHudEditor(it) })
    }

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        return showHelp(context)
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

    private fun addCarryee(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        val playerName = StringArgumentType.getString(ctx, "player")
        val count = IntegerArgumentType.getInteger(ctx, "count")

        val carryee = CarryCounter.addCarryee(playerName, count)
        if (carryee != null) {
            if (carryee.total == count) ChatUtils.addMessage("$prefix §fAdded §b$playerName§f for §b$count§f carries.")
            else ChatUtils.addMessage("$prefix §fUpdated §b$playerName§f to §b${carryee.total}§f total (§b${carryee.count}§f/§b${carryee.total}§f)")
        }
        return 1
    }

    private fun removeCarryee(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        val playerName = StringArgumentType.getString(ctx, "player")
        val removed = CarryCounter.removeCarryee(playerName)
        ChatUtils.addMessage("$prefix §f${if (removed) "Removed" else "Player not found:"} §b$playerName")
        return 1
    }

    private fun setTotal(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        val playerName = StringArgumentType.getString(ctx, "player")
        val total = IntegerArgumentType.getInteger(ctx, "total")

        val carryee = CarryCounter.findCarryee(playerName)
        if (carryee != null) {
            carryee.total = total
            ChatUtils.addMessage("$prefix §fSet §b$playerName§f total to §b$total§f (§b${carryee.count}§f/§b$total§f)")
        } else ChatUtils.addMessage("$prefix §fPlayer §b$playerName§f not found!")
        return 1
    }

    private fun setCount(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        val playerName = StringArgumentType.getString(ctx, "player")
        val count = IntegerArgumentType.getInteger(ctx, "count")

        val carryee = CarryCounter.findCarryee(playerName)
        if (carryee != null) {
            carryee.count = count
            ChatUtils.addMessage("$prefix §fSet §b$playerName§f count to §b$count§f (§b$count§f/§b${carryee.total}§f)")
            if (count >= carryee.total) carryee.complete()
        } else ChatUtils.addMessage("$prefix §fPlayer §b$playerName§f not found!")
        return 1
    }

    private fun clearCarryees(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        val count = CarryCounter.carryees.size
        CarryCounter.clearCarryees()
        ChatUtils.addMessage("$prefix §fCleared §b$count§f carries.")
        return 1
    }

    private fun listCarryees(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        if (CarryCounter.carryees.isEmpty()) {
            ChatUtils.addMessage("$prefix §fNo active carries.")
            return 1
        }

        ChatUtils.addMessage("$prefix §fActive Carries:")
        CarryCounter.carryees.forEach { carryee ->
            val progress = "§b${carryee.count}§f/§b${carryee.total}"
            val lastBoss = if (carryee.count > 0) "§7(${carryee.getTimeSinceLastBoss()} ago)" else ""
            ChatUtils.addMessage("§7> §b${carryee.name}§f - $progress $lastBoss")
        }
        return 1
    }

    private fun showLogs(ctx: CommandContext<FabricClientCommandSource>, page: Int): Int {
        if (!checkEnabled()) return 0

        val logs = CarryCounter.dataUtils.getData().completedCarries.sortedByDescending { it.timestamp }
        if (logs.isEmpty()) {
            ChatUtils.addMessage("$prefix §fNo carry logs found.")
            return 1
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

        ctx.source.sendFeedback(headerText)

        logs.subList(startIndex, endIndex).forEach { log ->
            val date = SimpleDateFormat("d/M/yyyy").format(Date(log.timestamp))
            val time = SimpleDateFormat("HH:mm").format(Date(log.timestamp))
            ChatUtils.addMessage("§7> §b${log.playerName} §7- §c$date §fat §c$time §7- §b${log.totalCarries} §fcarries")
        }

        ChatUtils.addMessage("§c§l| §fTotal carries: §b$totalCarries")
        ChatUtils.addMessage("§7⏤".repeat(40))
        return 1
    }

    private fun openHudEditor(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!checkEnabled()) return 0

        TickUtils.schedule(2) {
            mc.execute {
                mc.setScreen(HUDEditor())
            }
        }
        ChatUtils.addMessage("$prefix §fOpened HUD editor.")
        return 1
    }

    private fun showHelp(ctx: CommandContext<FabricClientCommandSource>): Int {
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
        return 1
    }
}