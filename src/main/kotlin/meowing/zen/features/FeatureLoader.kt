package meowing.zen.features

import meowing.zen.Zen.Companion.LOGGER
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis

object FeatureLoader {
    private var moduleCount = 0
    private var commandCount = 0
    private var loadtime: Long = 0

    val featureClassNames = FeatureLoader::class.java.getResourceAsStream("/features.list")?.use { stream ->
        stream.bufferedReader().use { reader ->
            reader.readLines()
        }
    } ?: emptyList()

    val commandClassNames = FeatureLoader::class.java.getResourceAsStream("/commands.list")?.use { stream ->
        stream.bufferedReader().use { reader ->
            reader.readLines()
        }
    } ?: emptyList()

    fun init() {
        val starttime = TimeUtils.now

        featureClassNames.forEach { className ->
            try {
                Class.forName(className)
                moduleCount++
            } catch (e: Exception) {
                LOGGER.error("Error loading module-$className: $e")
            }
        }

        commandClassNames.forEach { className ->
            try {
                Class.forName(className)
                commandCount++
            } catch (e: Exception) {
                LOGGER.error("Error initializing command-$className: $e")
                e.printStackTrace()
            }
        }

        loadtime = starttime.since.millis
    }

    fun getFeatCount(): Int = moduleCount
    fun getCommandCount(): Int = commandCount
    fun getLoadtime(): Long = loadtime
}