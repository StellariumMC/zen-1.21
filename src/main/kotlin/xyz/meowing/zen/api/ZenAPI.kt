package xyz.meowing.zen.api

import xyz.meowing.zen.Zen
import java.security.MessageDigest
import net.fabricmc.loader.api.FabricLoader
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@Zen.Module
object ZenAPI {
    private var ws: WebSocket? = null

    init {
        val uuid = (Zen.mc.session.uuidOrNull?: Zen.mc.session.username).toString()
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
        val client = HttpClient.newHttpClient()

        client.newWebSocketBuilder()
            .buildAsync(uri, object : WebSocket.Listener {
                override fun onOpen(webSocket: WebSocket) {
                    ws = webSocket
                    webSocket.request(1)
                }

                override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*> {
                    // Handle server messages if needed
                    webSocket.request(1)
                    return CompletableFuture.completedFuture(null)
                }

                override fun onError(webSocket: WebSocket?, error: Throwable) {
                    println("WebSocket error: $error")
                }

                override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String?): CompletionStage<*> {
                    println("WebSocket closed: $statusCode ${reason ?: ""}")
                    return CompletableFuture.completedFuture(null)
                }
            })
    }

    private fun getZenJarName(): String {
        val modsDir = FabricLoader.getInstance().gameDir.resolve("mods").toFile()
        val names = modsDir.listFiles { f -> f.isFile && f.name.endsWith(".jar", true) && f.name.contains("zen", true) }?.map { it.name }?.sorted() ?: emptyList()
        return if (names.isNotEmpty()) names.joinToString(",") else "Zen-DevAuthClient.jar"
    }
}
