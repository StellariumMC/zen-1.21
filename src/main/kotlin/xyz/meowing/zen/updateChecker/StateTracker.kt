@file:Suppress("PropertyName")

package xyz.meowing.zen.updateChecker

import xyz.meowing.zen.Zen

internal object StateTracker {
    var dontShowForVersion: String by Zen.saveData.string("dontShowForVersion")

    var isMessageShown = false
    var latestVersion: String? = null

    var githubUrl: String? = null
    var modrinthUrl: String? = null

    var githubDownloadUrl: String? = null
    var modrinthDownloadUrl: String? = null

    var forceUpdate = false

    // do NOT rename these variables, it WILL break the update checker
    data class GitHubRelease(
        val tag_name: String,
        val html_url: String,
        val prerelease: Boolean,
        val assets: List<GitHubAsset>
    )

    data class GitHubAsset(
        val name: String,
        val browser_download_url: String
    )

    data class ModrinthVersion(
        val id: String,
        val version_number: String,
        val date_published: String,
        val game_versions: List<String>,
        val loaders: List<String>,
        val status: String,
        val version_type: String,
        val files: List<ModrinthFile>
    )

    data class ModrinthFile(
        val url: String,
        val filename: String,
        val primary: Boolean
    )
}