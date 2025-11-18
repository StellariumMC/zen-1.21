package xyz.meowing.zen.features.general.chatToTitle

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Module
object ChatToTitle : Feature(
    "chatToTitle",
    skyblockOnly = true
){
    override fun addConfig() {
        ConfigManager.addFeature(
            "Chat to title",
            "Create title on chat message",
            "General",
            ConfigElement(
                "chatToTitle",
                ElementType.Switch(true)
            )
        )
    }

    val patternData = StoredFile("features/ChatToTitle")
    var patterns: List<ChatTitlePattern> by patternData.list("patterns", ChatTitlePattern.CODEC)

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val message = event.message.string.removeFormatting()

            val first = patterns.firstOrNull { it.matches(message) }
            if (first != null) displayTitle(first.title)
        }
    }

    private fun displayTitle(title: String) {
        showTitle(
            title = title,
            subtitle = null,
            duration = 2000,
            scale = 4.0f
        )
    }

    fun clearAllPatterns() {
        patterns = emptyList()
        patternData.forceSave()
    }

    fun addPattern(pattern: String, title: String, filterType: ChatToTitleType): Boolean {
        if (pattern.isBlank() || patterns.any { it.pattern == pattern && it.filterType == filterType }) return false
        return try {
            if (filterType == ChatToTitleType.REGEX) Pattern.compile(pattern)
            patterns = patterns + ChatTitlePattern(pattern, title, filterType)
            patternData.forceSave()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removePattern(index: Int): Boolean {
        if (index < 0 || index >= patterns.size) return false
        patterns = patterns.filterIndexed { i, _ -> i != index }
        patternData.forceSave()
        return true
    }

    fun updatePattern(index: Int, newPattern: String, newTitle: String, filterType: ChatToTitleType): Boolean {
        if (index < 0 || index >= patterns.size || newPattern.isBlank()) return false
        return try {
            if (filterType == ChatToTitleType.REGEX) Pattern.compile(newPattern)
            patterns = patterns.mapIndexed { i, pattern ->
                if (i == index) ChatTitlePattern(newPattern, newTitle, filterType) else pattern
            }
            patternData.forceSave()
            true
        } catch (_: Exception) {
            false
        }
    }
}