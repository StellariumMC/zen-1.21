package meowing.zen.utils

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import com.google.gson.*
import java.awt.Color
import java.lang.reflect.Type

class ColorTypeAdapter : JsonSerializer<Color>, JsonDeserializer<Color> {
    override fun serialize(src: Color, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        val value = (src.red shl 16) or (src.green shl 8) or src.blue
        val falpha = src.alpha.toDouble() / 255.0
        jsonObject.addProperty("value", value)
        jsonObject.addProperty("falpha", falpha)
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Color {
        val jsonObject = json.asJsonObject

        return when {
            jsonObject.has("value") && jsonObject.has("falpha") -> {
                val value = jsonObject.get("value").asInt
                val falpha = jsonObject.get("falpha").asDouble
                val r = (value shr 16) and 0xFF
                val g = (value shr 8) and 0xFF
                val b = value and 0xFF
                val a = (falpha * 255).toInt().coerceIn(0, 255)
                Color(r, g, b, a)
            }

            // backwards compat
            jsonObject.has("r") && jsonObject.has("g") && jsonObject.has("b") -> {
                val r = jsonObject.get("r")?.asInt ?: 255
                val g = jsonObject.get("g")?.asInt ?: 255
                val b = jsonObject.get("b")?.asInt ?: 255
                val a = jsonObject.get("a")?.asInt ?: 255
                Color(r, g, b, a)
            }

            else -> Color(255, 255, 255, 255)
        }
    }
}

class DataUtils<T: Any>(fileName: String, private val defaultObject: T) {
    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(Color::class.java, ColorTypeAdapter())
            .setPrettyPrinting()
            .create()

        private val autosaveIntervals = ConcurrentHashMap<DataUtils<*>, Long>()
        private var loopStarted = false
    }

    private val dataFile = File(FabricLoader.getInstance().configDir.toFile(), "Zen-1.21/${fileName}.json")
    private var data: T = loadData()
    private var lastSavedTime = System.currentTimeMillis()

    init {
        dataFile.parentFile.mkdirs()
        autosave(5)
        startAutosaveLoop()
    }

    private fun loadData(): T {
        return try {
            if (dataFile.exists()) {
                gson.fromJson(dataFile.readText(), defaultObject::class.java) ?: defaultObject
            } else defaultObject
        } catch (e: Exception) {
            println("Error loading data from ${dataFile.absolutePath}: ${e.message}")
            defaultObject
        }
    }

    @Synchronized
    fun save() {
        try {
            dataFile.writeText(gson.toJson(data))
        } catch (e: Exception) {
            println("Error saving data to ${dataFile.absolutePath}: ${e.message}")
            e.printStackTrace()
        }
    }

    fun autosave(intervalMinutes: Long = 5) {
        autosaveIntervals[this] = intervalMinutes * 60000
    }

    fun setData(newData: T) {
        data = newData
    }

    fun getData(): T = data

    private fun startAutosaveLoop() {
        if (loopStarted) return
        loopStarted = true
        LoopUtils.loop(10000) {
            val currentTime = System.currentTimeMillis()
            autosaveIntervals.forEach { (dataUtils, interval) ->
                if (currentTime - dataUtils.lastSavedTime < interval) return@forEach
                try {
                    val currentData = dataUtils.loadData()
                    if (currentData == dataUtils.data) return@forEach
                } catch (ignored: Exception) {}
                dataUtils.save()
                dataUtils.lastSavedTime = currentTime
            }
        }
    }
}