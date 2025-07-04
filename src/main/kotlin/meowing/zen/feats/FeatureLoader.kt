package meowing.zen.feats

import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.carrying.carryhud
import meowing.zen.feats.general.calculator
import meowing.zen.feats.slayers.slayerstatscommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

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
        "general.damagetracker",
        "slayers.MetadataHandler",
        "slayers.slayertimer",
        "slayers.slayerhighlight",
        "slayers.slayerstats",
        "slayers.vengdmg",
        "slayers.vengtimer",
        "slayers.lasertimer",
        "carrying.carrycounter",
        "dungeons.bloodtimer",
        "dungeons.cryptreminder",
        "dungeons.keyalert",
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
            }
        }
        
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            calculator.register(dispatcher)
        }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            carrycommand.register(dispatcher)
            slayerstatscommand.register(dispatcher)
        }
        carryhud.initialize()
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getFeatCount(): Int = moduleCount
    fun getLoadtime(): Long = loadtime
}