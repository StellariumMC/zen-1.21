package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.config.ConfigCommand
import meowing.zen.feats.carrying.CarryHUD
import meowing.zen.feats.carrying.carrycommand
import meowing.zen.feats.general.CalculatorCommand
import meowing.zen.feats.slayers.SlayerStatsCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.reflections.Reflections

object FeatureLoader {
    private var moduleCount = 0
    private var loadtime: Long = 0

    fun init() {
        val features = Reflections("meowing.zen").getTypesAnnotatedWith(Zen.Module::class.java)
        val starttime = System.currentTimeMillis()
        val categoryOrder = listOf("general", "slayers", "dungeons", "meowing", "noclutter")

        features.sortedWith(compareBy<Class<*>> { clazz ->
            val packageName = clazz.`package`.name
            val category = packageName.substringAfterLast(".")
            val normalizedCategory = if (category == "carrying") "slayers" else category
            categoryOrder.indexOf(normalizedCategory).takeIf { it != -1 } ?: Int.MAX_VALUE
        }.thenBy { it.name }).forEach { clazz ->
            try {
                Class.forName(clazz.name)
                moduleCount++
            } catch (e: Exception) {
                System.err.println("[Zen] Error initializing ${clazz.name}: $e")
                e.printStackTrace()
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