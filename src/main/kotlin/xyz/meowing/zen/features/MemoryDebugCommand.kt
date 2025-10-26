package xyz.meowing.zen.features

import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.LoopUtils
import xyz.meowing.zen.utils.TickUtils

@Zen.Command
object MemoryDebugCommand : Commodore("zenmemorydebug", "zmd") {
    private val clientWorldClass = "net.minecraft.class_638"
    
    init {
        runs {
            Zen.LOGGER.info("=== ClientWorld Reference Scan ===")
            
            val worldRefs = mutableListOf<String>()
            
            runCatching {
                scanObjectForWorlds(DataUtils, "DataUtils", worldRefs)
                scanObjectForWorlds(TickUtils, "TickUtils", worldRefs)
                scanObjectForWorlds(LoopUtils, "LoopUtils", worldRefs)
                scanObjectForWorlds(EventBus, "EventBus", worldRefs)
                
                Zen::class.java.getDeclaredField("features").let { field ->
                    field.isAccessible = true
                    (field.get(null) as? List<*>)?.forEachIndexed { idx, feature ->
                        feature?.let { scanObjectForWorlds(it, "Feature[$idx:${it::class.simpleName}]", worldRefs) }
                    }
                }
                
                if (worldRefs.isEmpty()) {
                    Zen.LOGGER.info("No ClientWorld references found")
                } else {
                    Zen.LOGGER.warn("Found ${worldRefs.size} ClientWorld references:")
                    worldRefs.forEach { Zen.LOGGER.warn("  $it") }
                }
            }.onFailure { 
                Zen.LOGGER.error("Error scanning for worlds", it)
            }
        }
    }

    private fun scanObjectForWorlds(obj: Any, path: String, results: MutableList<String>) {
        obj::class.java.declaredFields.forEach { field ->
            field.isAccessible = true
            runCatching {
                val value = field.get(obj) ?: return@runCatching
                val fieldPath = "$path.${field.name}"
                
                when {
                    value::class.java.name == clientWorldClass -> {
                        results.add("$fieldPath: ClientWorld@${System.identityHashCode(value)}")
                    }
                    value is Collection<*> -> {
                        value.filterNotNull().filter { it::class.java.name == clientWorldClass }.forEachIndexed { idx, world ->
                            results.add("$fieldPath[$idx]: ClientWorld@${System.identityHashCode(world)}")
                        }
                    }
                    value is Map<*, *> -> {
                        value.entries.forEach { (k, v) ->
                            if (v != null && v::class.java.name == clientWorldClass) {
                                results.add("$fieldPath[$k]: ClientWorld@${System.identityHashCode(v)}")
                            }
                        }
                    }
                    value is Array<*> -> {
                        value.filterNotNull().filter { it::class.java.name == clientWorldClass }.forEachIndexed { idx, world ->
                            results.add("$fieldPath[$idx]: ClientWorld@${System.identityHashCode(world)}")
                        }
                    }
                }
            }
        }
    }
}