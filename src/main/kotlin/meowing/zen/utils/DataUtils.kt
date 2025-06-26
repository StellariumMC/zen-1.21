package meowing.zen.utils

import com.google.gson.Gson
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DataUtils<T: Any>(fileName: String, private val defaultObject: T) {
    companion object {
        private val gson = Gson()
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
            defaultObject
        }
    }

    @Synchronized
    fun save() {
        try {
            dataFile.writeText(gson.toJson(data))
        } catch (e: Exception) {
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