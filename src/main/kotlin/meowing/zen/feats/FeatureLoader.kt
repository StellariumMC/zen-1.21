package meowing.zen.feats

import meowing.zen.config.ConfigCommand
import meowing.zen.feats.carrying.CarryHUD
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.general.CalculatorCommand
import meowing.zen.feats.slayers.SlayerStatsCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.reflections.Reflections

object FeatureLoader {
    private var moduleCount = 0
    private var moduleErr = 0
    private var loadtime: Long = 0

    fun init() {
        val starttime = System.currentTimeMillis()

        try {
            val reflections = Reflections("meowing.zen.feats")
            val featureClasses = reflections.getSubTypesOf(Feature::class.java)

            featureClasses.forEach { clazz ->
                try {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.isAccessible = true
                    constructor.newInstance()
                    moduleCount++
                } catch (e: Exception) {
                    System.err.println("[Zen] Error initializing ${clazz.simpleName}: $e")
                    e.printStackTrace()
                    moduleErr++
                }
            }
        } catch (e: Exception) {
            System.err.println("[Zen] Error during reflection scan: $e")
            e.printStackTrace()
            moduleErr++
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