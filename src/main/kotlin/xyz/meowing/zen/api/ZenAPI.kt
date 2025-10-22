package xyz.meowing.zen.api

import xyz.meowing.zen.Zen
import java.security.MessageDigest
import net.fabricmc.loader.api.FabricLoader
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.Zen.Companion.LOGGER
import xyz.meowing.zen.utils.LoopUtils
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.math.pow

@Zen.Module
object ZenAPI {
    private var ws: WebSocket? = null
    private var reconnectAttempts = 0
    private const val MAX_RECONNECT_ATTEMPTS = 5
    private const val BASE_RECONNECT_DELAY = 10_000L // 10 seconds

    init {
        connectToWebsocket()
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            LOGGER.warn("Max reconnection attempts ($MAX_RECONNECT_ATTEMPTS) reached. Stopping reconnection.")
            return
        }
        val delayMillis = (BASE_RECONNECT_DELAY * 2.0.pow(reconnectAttempts.toDouble())).toLong()
        reconnectAttempts++
        LOGGER.info("Reconnecting in ${delayMillis / 1000} seconds...")
        LoopUtils.setTimeout(delayMillis) {
            connectToWebsocket()
        }
    }

    fun connectToWebsocket() {
        LOGGER.info("Attempting to connect to WebSocket...")
        val uuid = (client.session.uuidOrNull ?: client.session.username).toString()
        val hashedUUID = MessageDigest.getInstance("MD5")
            .digest(uuid.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val debug: Map<String, String> = mapOf(
            "version" to Zen.modInfo.version,
            "gameVersion" to Zen.modInfo.mcVersion,
            "hashedUUID" to hashedUUID,
            "jarName" to getZenJarName()
        )

        val debugOptions = debug.entries.joinToString("&") { (k, v) -> "${URLEncoder.encode(k, StandardCharsets.UTF_8)}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}" }
        val uri = URI.create("ws://zen.mrfast-developer.com:1515/ws?$debugOptions")
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

        val connectionAttempt = client.newWebSocketBuilder()
            .buildAsync(uri, object : WebSocket.Listener {
                override fun onOpen(webSocket: WebSocket) {
                    LOGGER.info("WebSocket connected!")
                    ws = webSocket
                    reconnectAttempts = 0 // Reset on successful connection
                    webSocket.request(1)
                }

                override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*> {
                    webSocket.request(1)
                    return CompletableFuture.completedFuture(null)
                }

                override fun onError(webSocket: WebSocket?, error: Throwable) {
                    LOGGER.error("WebSocket error: $error")
                }

                override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String?): CompletionStage<*> {
                    LOGGER.info("WebSocket closed: $statusCode ${reason ?: ""}")
                    scheduleReconnect()
                    return CompletableFuture.completedFuture(null)
                }
            })

        // Handle connection timeout or failure
        connectionAttempt.exceptionally { error ->
            LOGGER.error("WebSocket connection failed: $error")
            scheduleReconnect()
            null
        }
    }

    private fun getZenJarName(): String {
        val modsDir = FabricLoader.getInstance().gameDir.resolve("mods").toFile()
        val names = modsDir.listFiles { f -> f.isFile && f.name.endsWith(".jar", true) && f.name.contains("zen", true) }?.map { it.name }?.sorted() ?: emptyList()
        return if (names.isNotEmpty()) names.joinToString(",") else "Zen-DevAuthClient.jar"
    }
}
