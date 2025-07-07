package meowing.zen.feats

import meowing.zen.config.ConfigCommand
import meowing.zen.feats.carrying.CarryHUD
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.general.CalculatorCommand
import meowing.zen.feats.slayers.SlayerStatsCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object FeatureLoader {
    private val features = arrayOf(
        "meowing.automeow",
        "meowing.meowdeathsounds",
        "meowing.meowsounds",
        "general.guildjoinleave",
        "general.friendjoinleave",
        "general.guildmessage",
        "general.partymessage",
        "general.worldage",
        "general.betterah",
        "general.betterbz",
        "general.blockoverlay",
        "general.customsize",
        "general.serveralert",
        "slayers.MetadataHandler",
        "slayers.slayertimer",
        "slayers.slayerhighlight",
        "slayers.slayerstats",
        "slayers.vengdmg",
        "slayers.vengtimer",
        "slayers.lasertimer",
        "slayers.minibossspawn",
        "carrying.carrycounter",
        "dungeons.bloodtimer",
        "dungeons.cryptreminder",
        "dungeons.keyalert",
        "dungeons.keyhighlight",
        "dungeons.partyfinder",
        "dungeons.serverlagtimer",
        "dungeons.termtracker",
        "dungeons.firefreeze",
        "dungeons.architectdraft",
        "dungeons.boxstarmobs",
        "dungeons.leapannounce",
        "noclutter.hidefallingblocks",
        "noclutter.hidefireoverlay",
        "noclutter.hidestatuseffects",
        "noclutter.nothunder"
    )

    private var moduleCount = 0
    private var moduleErr = 0
    private var loadtime: Long = 0

    fun init() {
        val starttime = System.currentTimeMillis()
        features.forEach { className ->
            try {
                val fullClassName = "meowing.zen.feats.$className"
                Class.forName(fullClassName)
                moduleCount++
            } catch (e: Exception) {
                System.err.println("[Zen] Error initializing $className: $e")
                e.printStackTrace()
                moduleErr++
            }
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            carrycommand.register(dispatcher)
            SlayerStatsCommand.register(dispatcher)
            ConfigCommand.register(dispatcher)
            CalculatorCommand.register(dispatcher)
        }

        CarryHUD.initialize()
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getFeatCount(): Int = moduleCount
    fun getLoadtime(): Long = loadtime
}