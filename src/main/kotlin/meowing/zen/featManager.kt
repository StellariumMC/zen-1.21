package meowing.zen

import meowing.zen.config.zencfg
import meowing.zen.utils.EventBus
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger
import java.nio.file.*
import java.lang.reflect.Field
import java.io.IOException

object featManager {
    private val LOGGER = Logger.getLogger("Zen")
    private val features = ConcurrentHashMap<String, FeatureHandler>()
    private val configState = ConcurrentHashMap<String, Boolean>()
    private val fieldCache = ConcurrentHashMap<String, Field>()
    private const val featpath = "/meowing/zen/feats"
    private val immutableFeats = ConcurrentHashMap.newKeySet<String>()
    private val moduleCount = AtomicInteger(0)

    init {
        cacheConf()
    }

    private fun cacheConf() {
        val fields = zencfg::class.java.declaredFields
        for (field in fields) {
            if (field.type == Boolean::class.javaPrimitiveType) {
                field.isAccessible = true
                fieldCache[field.name] = field
            }
        }
    }

    private class FeatureHandler(
        val owner: Any,
        val registerHandler: Runnable
    ) {
        @Volatile
        var isActive = false

        fun toggle(active: Boolean): Boolean {
            return when {
                active && !isActive -> {
                    try {
                        registerHandler.run()
                        isActive = true
                        true
                    } catch (e: Exception) {
                        LOGGER.warning("Failed to activate feature: ${owner::class.java.simpleName} - ${e.message}")
                        false
                    }
                }
                !active && isActive -> {
                    try {
                        EventBus.unregister(owner)
                        isActive = false
                        true
                    } catch (e: Exception) {
                        LOGGER.warning("Failed to deactivate feature: ${owner::class.java.simpleName} - ${e.message}")
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun autoDiscFeats() {
        val resource = featManager::class.java.getResource(featpath)
        if (resource == null) {
            LOGGER.warning("Features directory not found: $featpath")
            return
        }

        try {
            val uri = resource.toURI()
            if (uri.scheme == "jar") {
                getOrCreateFileSystem(uri).use { fs ->
                    discoverFeats(fs.getPath(featpath))
                }
            } else {
                discoverFeats(Paths.get(uri))
            }
        } catch (e: Exception) {
            LOGGER.severe("Failed to discover features: ${e.message}")
        }
    }

    private fun getOrCreateFileSystem(uri: java.net.URI): FileSystem {
        return try {
            FileSystems.getFileSystem(uri)
        } catch (e: FileSystemNotFoundException) {
            FileSystems.newFileSystem(uri, emptyMap<String, Any>())
        }
    }

    private fun discoverFeats(path: Path) {
        try {
            Files.walk(path, 3).use { stream ->
                stream.filter { p ->
                    val pathStr = p.toString()
                    pathStr.endsWith(".class") && pathStr.contains("meowing/zen/feats")
                }
                    .parallel()
                    .forEach(::loadFeats)
            }
        } catch (e: IOException) {
            LOGGER.severe("Failed to walk features directory: ${e.message}")
        }
    }

    private fun loadFeats(classPath: Path) {
        try {
            val pathStr = classPath.toString()
            val startIdx = pathStr.indexOf("meowing/zen/feats")
            if (startIdx == -1) return

            val className = pathStr.substring(startIdx)
                .replace('/', '.')
                .replace('\\', '.')
                .replace(".class", "")

            val clazz = Class.forName(className)
            val initMethod = clazz.getDeclaredMethod("initialize")
            initMethod.invoke(null)

            moduleCount.incrementAndGet()
        } catch (e: ReflectiveOperationException) {
            LOGGER.fine("Skipping non-feature class or initialization failed: $classPath")
        } catch (e: Exception) {
            LOGGER.warning("Unexpected error loading feature class $classPath: ${e.message}")
        }
    }

    fun register(owner: Any, registerHandler: Runnable) {
        Objects.requireNonNull(owner, "Feature owner cannot be null")
        Objects.requireNonNull(registerHandler, "Register handler cannot be null")

        val name = owner::class.java.simpleName.lowercase()
        val handler = FeatureHandler(owner, registerHandler)

        features[name] = handler
        immutableFeats.add(name)
        val shouldBeActive = getConfigValue(name)
        configState[name] = shouldBeActive
        handler.toggle(shouldBeActive)
    }

    fun onConfigChange() {
        features.entries.parallelStream().forEach { (name, handler) ->
            val shouldBeActive = getConfigValue(name)
            val lastValue = configState[name]

            if (lastValue == null || lastValue != shouldBeActive) {
                if (handler.toggle(shouldBeActive)) {
                    configState[name] = shouldBeActive
                }
            }
        }
    }

    private fun getConfigValue(fieldName: String): Boolean {
        val field = fieldCache[fieldName] ?: return false

        return try {
            field.get(Zen.getConfig()) as Boolean
        } catch (e: IllegalAccessException) {
            LOGGER.warning("Failed to access config field: $fieldName")
            false
        } catch (e: ClassCastException) {
            LOGGER.warning("Config field is not boolean: $fieldName")
            false
        }
    }

    fun initAll() {
        autoDiscFeats()
    }

    fun getFeatCount(): Int {
        return features.values.stream()
            .mapToInt { handler -> if (handler.isActive) 1 else 0 }
            .sum()
    }

    fun getRegFeats(): Set<String> {
        return immutableFeats
    }

    fun getModuleCount(): Int {
        return moduleCount.get()
    }
}