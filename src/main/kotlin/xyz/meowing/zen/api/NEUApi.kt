package xyz.meowing.zen.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.api.data.StoredFile
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import xyz.meowing.zen.Zen.LOGGER
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.InternalEvent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Module
object NEUApi {
    private const val NEU_ZIP_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-Repo/archive/master.zip"
    private var client: CloseableHttpClient = HttpClients.createDefault()
    private val isDownloading = AtomicBoolean(false)

    private val neuItemFile = StoredFile("api/NeuAPI/NeuItems")
    private val neuMobFile = StoredFile("api/NeuAPI/NeuMobs")
    private val neuConstantFile = StoredFile("api/NeuAPI/NeuConstants")
    private val eTagFile = StoredFile("api/NeuAPI/NEUAPI-ETAG")

    var neuItemData by neuItemFile.jsonObject("data")
    var neuMobData by neuMobFile.jsonObject("data")
    var neuConstantData by neuConstantFile.jsonObject("data")
    private var eTagData by eTagFile.jsonObject("data")

    fun downloadAndProcessRepo(force: Boolean = false) {
        if (!isDownloading.compareAndSet(false, true)) {
            return
        }

        try {
            val eTagString = eTagData.get("tag")?.asString ?: ""

            val request = HttpHead(NEU_ZIP_URL).apply {
                if (eTagString.isNotEmpty()) {
                    setHeader("If-None-Match", eTagString)
                }
            }

            val response = client.execute(request)
            val matchesLastETag = response.statusLine.statusCode == 304

            if (!matchesLastETag || force) {
                client.execute(HttpGet(NEU_ZIP_URL))
                    .takeIf { it.statusLine.statusCode == 200 }?.entity?.content?.use { zipStream ->
                        ZipInputStream(zipStream).use { zip ->
                            var entry: ZipEntry? = zip.nextEntry

                            val neuItems = JsonObject()
                            val neuMobs = JsonObject()
                            val neuConstants = JsonObject()

                            while (entry != null) {
                                if (entry.name.endsWith(".json")) {
                                    val jsonContent = zip.bufferedReader().readText()
                                    val name = entry.name.split("/").last().removeSuffix(".json")

                                    try {
                                        val value = JsonParser().parse(jsonContent).asJsonObject

                                        when {
                                            entry.name.contains("/items/") -> neuItems.add(name, value)
                                            entry.name.contains("/mobs/") -> neuMobs.add(name, value)
                                            entry.name.contains("/constants/") -> neuConstants.add(name, value)
                                        }
                                    } catch (e: Exception) {
                                        LOGGER.error("Failed to parse JSON from entry ${entry.name}, skipping...")
                                    }
                                }
                                entry = zip.nextEntry
                            }

                            neuItemData = neuItems
                            neuMobData = neuMobs
                            neuConstantData = neuConstants

                            zip.closeEntry()
                            LOGGER.info("NEU API data downloaded and processed successfully.")

                            val newETag = response.getFirstHeader("ETag")?.value
                            eTagData = JsonObject().apply {
                                addProperty("tag", newETag ?: "")
                            }

                            LOGGER.info("Saved NEU API Data to file and updated ETag.")

                            neuItemFile.forceSave()
                            neuMobFile.forceSave()
                            neuConstantFile.forceSave()
                            eTagFile.forceSave()
                        }
                    }
            } else {
                LOGGER.info("ETag matches. No need to download. Loading from file...")

                if (neuItemData.entrySet().isEmpty() || neuMobData.entrySet().isEmpty() || neuConstantData.entrySet().isEmpty()) {
                    LOGGER.warn("Failed to load NEU API data from file. Redownloading...")
                    isDownloading.set(false)
                    downloadAndProcessRepo(true)
                    return
                }
            }
        } finally {
            isDownloading.set(false)
            EventBus.post(InternalEvent.NeuAPI.Load())
        }
    }
}