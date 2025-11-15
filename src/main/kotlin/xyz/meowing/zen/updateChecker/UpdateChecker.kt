package xyz.meowing.zen.updateChecker

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.GITHUB_REPO
import xyz.meowing.zen.Zen.MODRINTH_PROJECT_ID
import xyz.meowing.zen.utils.NetworkUtils.createConnection
import java.util.concurrent.CompletableFuture

object UpdateChecker {
    fun check() {
        CompletableFuture.supplyAsync {
            val github = checkGitHub()
            val modrinth = checkModrinth()
            val latest = listOfNotNull(github?.first, modrinth?.first)
                .maxByOrNull { compareVersions(it, Zen.modInfo.version) }
                ?: return@supplyAsync KnitChat.fakeMessage("${github?.first}, ${modrinth?.first}")

            if (
                (compareVersions(latest, Zen.modInfo.version) > 0 && latest != StateTracker.dontShowForVersion) ||
                StateTracker.forceUpdate
                ) {
                StateTracker.isMessageShown = true
                StateTracker.forceUpdate = false
                StateTracker.latestVersion = latest
                StateTracker.githubUrl = github?.second
                StateTracker.githubDownloadUrl = github?.third
                StateTracker.modrinthUrl = modrinth?.second
                StateTracker.modrinthDownloadUrl = modrinth?.third
                TickScheduler.Client.post {
                    client.setScreen(UpdateGUI())
                }
            }
        }
    }

    private fun checkGitHub(): Triple<String, String, String?>? = runCatching {
        val connection = createConnection("https://api.github.com/repos/${GITHUB_REPO}/releases")
        connection.requestMethod = "GET"

        if (connection.responseCode == 200) {
            val releases: List<StateTracker.GitHubRelease> = Gson().fromJson(
                connection.inputStream.reader(),
                object : TypeToken<List<StateTracker.GitHubRelease>>() {}.type
            )

            releases.firstOrNull { !it.prerelease }?.let { release ->
                val downloadUrl = release.assets.firstOrNull { it.name.endsWith(".jar") }?.browser_download_url
                Triple(release.tag_name.replace("v", ""), release.html_url, downloadUrl)
            }
        } else null
    }.getOrNull()

    private fun checkModrinth(): Triple<String, String, String?>? = runCatching {
        val connection = createConnection("https://api.modrinth.com/v2/project/${MODRINTH_PROJECT_ID}/version")
        connection.requestMethod = "GET"

        if (connection.responseCode == 200) {
            val versions: List<StateTracker.ModrinthVersion> = Gson().fromJson(
                connection.inputStream.reader(),
                object : TypeToken<List<StateTracker.ModrinthVersion>>() {}.type
            )

            val filteredVersions =
                versions.filter {
                    it.loaders.contains("fabric") && it.status == "listed" && it.version_type == "release" && it.game_versions.contains(KnitClient.minecraftVersion)
                }

            filteredVersions.maxByOrNull { it.date_published }?.let { version ->
                val primaryFile = version.files.firstOrNull { it.primary } ?: version.files.firstOrNull()
                primaryFile?.let {
                    Triple(version.version_number, "https://modrinth.com/mod/${MODRINTH_PROJECT_ID}/version/${version.id}", it.url)
                }
            }
        } else null
    }.getOrNull()

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