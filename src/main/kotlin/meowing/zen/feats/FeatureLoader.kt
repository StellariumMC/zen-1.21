package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.reflections.Reflections

object FeatureLoader {
    private var moduleCount = 0
    private var commandCount = 0
    private var loadtime: Long = 0

    fun init() {
        val reflections = Reflections("meowing.zen")

        val features = reflections.getTypesAnnotatedWith(Zen.Module::class.java)
        val starttime = TimeUtils.now
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

        val commands = reflections.getTypesAnnotatedWith(Zen.Command::class.java)
        commandCount = commands.size
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commands.forEach { commandClass ->
                try {
                    val commandInstance = commandClass.getDeclaredField("INSTANCE").get(null)
                    val registerMethod = commandClass.methods.find { it.name == "register" } // a bit eh but it works
                    registerMethod?.invoke(commandInstance, dispatcher)
                } catch (e: Exception) {
                    System.err.println("[Zen] Error initializing ${commandClass.name}: $e")
                    e.printStackTrace()
                }
            }
        }

        loadtime = starttime.since.millis
    }

    fun getFeatCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}