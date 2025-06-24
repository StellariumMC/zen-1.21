package meowing.zen.feats

import meowing.zen.feats.general.calculator
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
        "slayers.MetadataHandler",
        "slayers.slayertimer",
        "slayers.slayerhighlight"
    )

    private var moduleCount = 0
    private var moduleErr = 0
    private var commandCount = 0
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
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> calculator.register(dispatcher) }
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getFeatCount(): Int = moduleCount
    fun getLoadtime(): Long = loadtime
}