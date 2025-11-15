package xyz.meowing.zen.updateChecker

import com.google.common.base.Preconditions
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.Util
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.core.VexelScreen
import xyz.meowing.vexel.elements.Button
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.ui.Theme
import xyz.meowing.zen.utils.NetworkUtils
import java.awt.Desktop
import java.io.File
import java.net.URI

class UpdateGUI : VexelScreen("Zen Update") {
    private var downloadState = DownloadState.NotStarted
    private var downloadButton: Button? = null
    private var progressBar: Rectangle? = null
    private var progressFill: Rectangle? = null
    private var progressText: Text? = null

    override fun afterInitialization() {
        val mainContainer = Rectangle(Theme.BgDark.color, Theme.Border.color, 8f, 1f)
            .setSizing(30f, Size.ParentPerc, 40f, Size.ParentPerc)
            .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
            .padding(16f)
            .dropShadow(20f, 2f, Theme.BgDark.color)
            .childOf(window)

        createHeader(mainContainer)
        createContent(mainContainer)
        createFooter(mainContainer)
    }

    override fun onCloseGui() {
        super.onCloseGui()
        StateTracker.dontShowForVersion = StateTracker.latestVersion ?: ""
    }

    private fun createHeader(parent: Rectangle) {
        val header = Container()
            .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(parent)

        Text("Zen | Update Available", Theme.Success.color, 24f)
            .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
            .setOffset(0f, -8f)
            .childOf(header)

        Button("×", Theme.Text.color, fontSize = 16f)
            .setSizing(32f, Size.Pixels, 32f, Size.Pixels)
            .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
            .setOffset(0f, -8f)
            .alignRight()
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(4f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                minecraft?.setScreen(null)
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.Danger.color, 150L) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color, 150L) }
                )
            }
            .childOf(header)

        Rectangle(Theme.Border.color, 0, 0f, 0f)
            .setSizing(100f, Size.ParentPerc, 1f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 4f, Pos.AfterSibling)
            .childOf(header)
    }

    private fun createContent(parent: Rectangle) {
        val content = Container()
            .setSizing(100f, Size.ParentPerc, 70f, Size.Fill)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(parent)

        val versionContainer = Container()
            .setSizing(80f, Size.ParentPerc, 0f, Size.Auto)
            .setPositioning(0f, Pos.ParentCenter, 20f, Pos.ParentPixels)
            .childOf(content)

        Text("Current Version", Theme.TextMuted.color, 14f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(versionContainer)

        Text("v${Zen.modInfo.version}", 0xFFF87171.toInt(), 18f)
            .setPositioning(0f, Pos.ParentPixels, 24f, Pos.ParentPixels)
            .childOf(versionContainer)

        Text("Latest Version", Theme.TextMuted.color, 14f)
            .setPositioning(0f, Pos.ParentPixels, 64f, Pos.ParentPixels)
            .childOf(versionContainer)

        Text("v${StateTracker.latestVersion}", Theme.Success.color, 18f)
            .setPositioning(0f, Pos.ParentPixels, 88f, Pos.ParentPixels)
            .childOf(versionContainer)

        val buttonContainer = Container()
            .setSizing(80f, Size.ParentPerc, 0f, Size.Auto)
            .setPositioning(0f, Pos.ParentCenter, 140f, Pos.ParentPixels)
            .childOf(content)

        val downloadUrl = StateTracker.modrinthDownloadUrl ?: StateTracker.githubDownloadUrl
        downloadUrl?.let { url ->
            downloadButton = createDirectDownloadButton { downloadMod(url) }
                .setSizing(100f, Size.ParentPerc, 36f, Size.Pixels)
                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
                .childOf(buttonContainer)

            createProgressBar()
                .setSizing(100f, Size.ParentPerc, 6f, Size.Pixels)
                .setPositioning(0f, Pos.ParentPixels, 48f, Pos.ParentPixels)
                .childOf(buttonContainer)
        }

        val webButtonContainer = Container()
            .setSizing(100f, Size.ParentPerc, 36f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, if (downloadUrl != null) 66f else 0f, Pos.ParentPixels)
            .childOf(buttonContainer)

        StateTracker.modrinthUrl?.let { url ->
            createWebButton("Modrinth") { openUrl(url) }
                .setSizing(if (StateTracker.githubUrl != null) 48f else 100f, Size.ParentPerc, 100f, Size.ParentPerc)
                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
                .childOf(webButtonContainer)
        }

        StateTracker.githubUrl?.let { url ->
            createWebButton("GitHub") { openUrl(url) }
                .setSizing(if (StateTracker.modrinthUrl != null) 48f else 100f, Size.ParentPerc, 100f, Size.ParentPerc)
                .setPositioning(if (StateTracker.modrinthUrl != null) 52f else 0f, Pos.ParentPercent, 0f, Pos.ParentPixels)
                .childOf(webButtonContainer)
        }
    }

    private fun createFooter(parent: Rectangle) {
        Rectangle(Theme.Border.color, 0, 0f, 0f)
            .setSizing(100f, Size.ParentPerc, 1f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(parent)

        val footer = Container()
            .setSizing(60f, Size.ParentPerc, 48f, Size.Pixels)
            .setPositioning(0f, Pos.ParentCenter, 0f, Pos.AfterSibling)
            .childOf(parent)

        Button("Don't Show Again", Theme.Text.color, fontSize = 13f)
            .setSizing(48f, Size.ParentPerc, 36f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                StateTracker.dontShowForVersion = StateTracker.latestVersion ?: ""
                minecraft?.setScreen(null)
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.BgLight.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color) }
                )
            }
            .childOf(footer)

        Button("Later", Theme.Text.color, fontSize = 13f)
            .setSizing(48f, Size.ParentPerc, 36f, Size.Pixels)
            .setPositioning(52f, Pos.ParentPercent, 0f, Pos.ParentCenter)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                minecraft?.setScreen(null)
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.BgLight.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color) }
                )
            }
            .childOf(footer)
    }

    private fun createProgressBar(): Container {
        val container = Container()
            .childOf(window)

        progressBar = Rectangle(Theme.BgLight.color, 0, 3f, 0f)
            .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(container)

        progressFill = Rectangle(Theme.Primary.color, 0, 3f, 0f)
            .setSizing(0f, Size.ParentPerc, 100f, Size.ParentPerc)
            .childOf(progressBar!!)

        progressText = Text("download the update :3", Theme.TextMuted.color, 12f)
            .setPositioning(0f, Pos.ParentCenter, 68f, Pos.ParentPixels)
            .childOf(container)

        container.hide()
        return container
    }

    private fun updateProgress(progress: Int, downloaded: Long, total: Long) {
        (progressBar?.parent as? VexelElement<*>)?.show()
        progressFill?.width = progress.toFloat()
        progressText?.text = "$progress% • ${formatBytes(downloaded)} / ${formatBytes(total)}"

        if (progress == 100) {
            progressBar?.hide()
            progressFill?.hide()
            progressText?.text = "yippee :3"
        }
    }

    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> "%.1f MB".format(mb)
            kb >= 1.0 -> "%.1f KB".format(kb)
            else -> "$bytes B"
        }
    }

    private fun createDirectDownloadButton(onClick: () -> Unit): Button {
        return Button("Download & Install", Theme.Text.color, fontSize = 14f)
            .backgroundColor(Theme.Success.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                if (downloadState == DownloadState.NotStarted) onClick()
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ ->
                        if (downloadState == DownloadState.NotStarted) {
                            this.background.colorTo(0xFF3E8E5E.toInt(), 150L)
                        }
                    },
                    onExit = { _, _ ->
                        if (downloadState == DownloadState.NotStarted) {
                            this.background.colorTo(Theme.Success.color, 150L)
                        }
                    }
                )
            }
    }

    private fun createWebButton(text: String, onClick: () -> Unit): Button {
        return Button(text, Theme.Text.color, fontSize = 13f)
            .backgroundColor(Theme.Bg.color)
            .borderColor(Theme.Border.color)
            .borderRadius(6f)
            .hoverColors(null, null)
            .onClick { _, _, _ ->
                onClick()
                true
            }
            .apply {
                onHover(
                    onEnter = { _, _ -> this.background.colorTo(Theme.BgLight.color) },
                    onExit = { _, _ -> this.background.colorTo(Theme.Bg.color) }
                )
            }
    }

    private fun tryDeleteCurrentFile(modsDir: File, isWindows: Boolean): File? {
        return modsDir.listFiles()?.find {
            it.isFile && it.name.contains("zen", ignoreCase = true) && it.extension.equals("jar", ignoreCase = true)
        }?.let {
            if (!isWindows) {
                try {
                    if (it.delete()) return null
                } catch (t: Throwable) {
                    Zen.LOGGER.error("Unable to delete old mod file '{}', scheduling for deletion on exit!", it.absolutePath, t)
                }
            }

            Zen.LOGGER.debug("Scheduling for deletion: {}", it.absolutePath)
            val pid = ProcessHandle.current().pid()

            if (isWindows) {
                val vbs = File.createTempFile("delete_old_mod", ".vbs")
                vbs.writeText("""
                    Set objWMIService = GetObject("winmgmts:\\.\root\cimv2")
                    Do
                        WScript.Sleep 2000
                        Set colProcesses = objWMIService.ExecQuery("SELECT * FROM Win32_Process WHERE ProcessId = $pid")
                    Loop While colProcesses.Count > 0

                    Set fso = CreateObject("Scripting.FileSystemObject")
                    fso.DeleteFile "${it.absolutePath}"
                    fso.DeleteFile "${vbs.absolutePath}"
                """.trimIndent())
                Runtime.getRuntime().exec(arrayOf("cscript", "//nologo", vbs.absolutePath))
            } else {
                val sh = File.createTempFile("delete_old_mod", ".sh")
                sh.writeText("""
                    #!/bin/sh
                    
                    while kill -s 0 $pid 2>/dev/null; do
                        sleep 2
                    done
                    
                    rm -f "${it.absolutePath}"
                    rm -- "$0"
                """.trimIndent())
                sh.setExecutable(true)
                Runtime.getRuntime().exec(arrayOf("/bin/sh", "${sh.absolutePath}"))
            }
            return it
        }
    }

    private fun downloadMod(downloadUrl: String) {
        if (downloadState != DownloadState.NotStarted) return
        downloadState = DownloadState.InProgress

        val mcVersion = KnitClient.minecraftVersion

        val loader = "fabric"
        val latestVersion = StateTracker.latestVersion

        downloadButton?.apply {
            innerText.text = "Preparing..."
            background.backgroundColor(Theme.Bg.color)
        }

        val modsDir = FabricLoader.getInstance().gameDir.resolve("mods").toFile().canonicalFile
        if (!modsDir.exists()) modsDir.mkdirs()

        val isWindows = Util.getPlatform() == Util.OS.WINDOWS
        val existingFile = tryDeleteCurrentFile(modsDir, isWindows)

        var fileName = "zen-${mcVersion}-${loader}-${latestVersion}.jar"
        if (existingFile?.let { it.exists() && it.name.equals(fileName, ignoreCase = isWindows) } == true) {
            fileName = "zen-${mcVersion}-${loader}-${latestVersion}-1.jar"
        }

        val outputFile = modsDir.resolve(fileName)
        Preconditions.checkArgument(
            outputFile.canonicalFile.startsWith(modsDir),
            "output file %s resolved to outside the mods directory, this is not allowed!",
            outputFile.canonicalPath
        )

        NetworkUtils.downloadFile(
            url = downloadUrl,
            outputFile = outputFile,
            onProgress = { downloaded, contentLength ->
                if (contentLength > 0) {
                    val progress = ((downloaded * 100) / contentLength).toInt()
                    updateProgress(progress, downloaded, contentLength)
                }
            },
            onComplete = {
                TickScheduler.Client.post {
                    downloadButton?.apply {
                        innerText.text = "Downloaded!"
                        background.backgroundColor(Theme.Success.color)
                    }
                    (progressBar?.parent as? VexelElement<*>)?.hide()

                    TickScheduler.Client.schedule(40) {
                        KnitChat.fakeMessage("$prefix §aUpdate downloaded! New version will be loaded when the game restarts.")
                        minecraft?.setScreen(null)
                    }
                }
                downloadState = DownloadState.Complete
            },
            onError = { exception ->
                TickScheduler.Client.post {
                    downloadButton?.apply {
                        innerText.text = "Error"
                        background.backgroundColor(Theme.Danger.color)
                    }
                    (progressBar?.parent as? VexelElement<*>)?.hide()
                    KnitChat.fakeMessage("$prefix §cDownload error: ${exception.message}")

                    TickScheduler.Client.schedule(60) {
                        resetDownloadButton()
                    }
                }
                downloadState = DownloadState.Error
            }
        ).also {
            TickScheduler.Client.post {
                downloadButton?.innerText?.text = "Downloading..."
            }
        }
    }

    private fun resetDownloadButton() {
        downloadState = DownloadState.NotStarted
        downloadButton?.apply {
            innerText.text = "Download & Install"
            background.backgroundColor(Theme.Success.color)
        }
        progressFill?.width = 0f
        progressText?.text = ""
    }

    private fun openUrl(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (_: Exception) {
        }
    }
}