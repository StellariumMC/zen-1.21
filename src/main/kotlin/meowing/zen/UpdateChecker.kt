package meowing.zen

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import meowing.zen.Zen.Companion.mc
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.CompletableFuture

object UpdateChecker {
    private const val current = "1.0.0"
    private var lastCheck = 0L
    private var isMessageShown = false

    data class GitHubRelease(val tag_name: String, val html_url: String, val prerelease: Boolean)
    data class ModrinthVersion(val id: String, val version_number: String, val date_published: String, val game_versions: List<String>, val loaders: List<String>, val status: String, val version_type: String)

    fun checkForUpdates() {
        if (System.currentTimeMillis() - lastCheck < 300000 || isMessageShown) return
        lastCheck = System.currentTimeMillis()

        CompletableFuture.supplyAsync {
            val github = checkGitHub()
            val modrinth = checkModrinth()

            val latest = listOfNotNull(github?.first, modrinth?.first)
                .maxByOrNull { compareVersions(it, current) } ?: return@supplyAsync

            if (compareVersions(latest, current) > 0) {
                isMessageShown = true
                val message = Text.literal("§c[Zen] §fUpdate available! §c$current §f-> §c$latest")

                val downloadMsg = Text.literal("§c[Zen] §fDownload: ")

                modrinth?.second?.let { url ->
                    val modrinthBtn = Text.literal("§a[Modrinth]")
                        .styled { it.withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
                            .withHoverEvent((HoverEvent.ShowText(Text.literal("Open Modrinth")))) }
                    downloadMsg.append(modrinthBtn)
                }

                if (github?.second != null && modrinth?.second != null)
                    downloadMsg.append(Text.literal("§f | "))

                github?.second?.let { url ->
                    val githubBtn = Text.literal("§b[GitHub]")
                        .styled { it.withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
                            .withHoverEvent((HoverEvent.ShowText(Text.literal("Open GitHub")))) }
                    downloadMsg.append(githubBtn)
                }

                mc.player!!.sendMessage(message, false)
                mc.player!!.sendMessage(downloadMsg, false)
            }
        }
    }

    private fun checkGitHub(): Pair<String, String>? {
        return try {
            val conn = URL("https://api.github.com/repos/kiwidotzip/zen-1.21/releases").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Zen")
            conn.connectTimeout = 10000
            conn.readTimeout = 30000

            if (conn.responseCode == 200) {
                val releases: List<GitHubRelease> = Gson().fromJson(conn.inputStream.reader(),
                    object : TypeToken<List<GitHubRelease>>() {}.type)
                releases.firstOrNull { !it.prerelease }?.let {
                    it.tag_name.replace("v", "") to it.html_url
                }
            } else null
        } catch (e: Exception) { null }
    }

    private fun checkModrinth(): Pair<String, String>? {
        return try {
            val conn = URL("https://api.modrinth.com/v2/project/zenmod/version").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Zen")
            conn.connectTimeout = 10000
            conn.readTimeout = 30000

            if (conn.responseCode == 200) {
                val versions: List<ModrinthVersion> = Gson().fromJson(conn.inputStream.reader(),
                    object : TypeToken<List<ModrinthVersion>>() {}.type)
                versions.filter {
                    it.loaders.contains("fabric") && it.status == "listed" && it.version_type == "release" &&
                            (it.game_versions.contains("1.21.5") || it.game_versions.contains("1.21.6"))
                }.maxByOrNull { it.date_published }?.let {
                    it.version_number to "https://modrinth.com/mod/zenmod/version/${it.id}"
                }
            } else null
        } catch (e: Exception) { null }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.replace(Regex("[^0-9.]"), "").split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.replace(Regex("[^0-9.]"), "").split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}