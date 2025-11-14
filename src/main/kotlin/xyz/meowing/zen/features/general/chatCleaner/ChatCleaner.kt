package xyz.meowing.zen.features.general.chatCleaner

import com.google.gson.Gson
import net.minecraft.client.gui.screens.ChatScreen
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.input.KnitKey
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.Zen.LOGGER
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.KeyEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.mixins.AccessorChatComponent
import java.util.regex.Pattern

@Module
object ChatCleaner : Feature(
    "chatCleaner"
) {
    private val chatCleanerKey by ConfigDelegate<Int>("chatCleaner.keybind")
    val patternData = StoredFile("features/ChatCleaner")
    var patterns: List<ChatPattern> by patternData.list("patterns", ChatPattern.CODEC)

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Chat cleaner",
                "Filter out unwanted chat messages using custom patterns",
                "General",
                ConfigElement(
                    "chatCleaner",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Keybind to add message to filter",
                ConfigElement(
                    "chatCleaner.keybind",
                    ElementType.Keybind(KnitKeys.KEY_H.code)
                )
            )
            .addFeatureOption(
                "Chat cleaner filter GUI",
                ConfigElement(
                    "chatCleaner.guiButton",
                    ElementType.Button("Open Filter GUI") {
                        TickScheduler.Client.post {
                            client.setScreen(ChatCleanerGui())
                        }
                    }
                )
            )
    }

    init {
        loadDefault()
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val message = event.message.stripped
            if (patterns.any { it.matches(message) }) event.cancel()
        }

        register<KeyEvent.Press> { _ ->
            if (client.screen !is ChatScreen || !KnitKey(chatCleanerKey).isPressed) return@register

            val chat = client.gui.chat as AccessorChatComponent
            val line = chat.getMessageLineIdx(chat.toChatLineMX(KnitMouse.Scaled.x), chat.toChatLineMY(KnitMouse.Scaled.y))

            if (line >= 0 && line < chat.visibleMessages.size && line < chat.messages.size) {
                val text = chat.messages[line].content().stripped

                if (text.isNotEmpty()) {
                    addPattern(text, ChatFilterType.EQUALS)
                    KnitChat.fakeMessage("$prefix §fAdded §7\"§c$text§7\" §fto filter.")
                }
            }
        }
    }

    fun loadDefault() {
        if (patterns.isEmpty()) {
            try {
                javaClass.getResourceAsStream("/assets/zen/chatfilter.json")?.use { stream ->
                    val defaultPatterns = Gson().fromJson(
                        stream.bufferedReader().readText(), Array<String>::class.java
                    )
                    patterns = defaultPatterns.map { ChatPattern(it, ChatFilterType.REGEX) }
                    patternData.forceSave()
                }
            } catch (e: Exception) {
                LOGGER.warn("Caught error while trying to load defaults in ChatCleaner: $e")
            }
        }
    }

    fun addPattern(pattern: String, filterType: ChatFilterType): Boolean {
        if (pattern.isBlank() || patterns.any { it.pattern == pattern && it.filterType == filterType }) return false
        return try {
            if (filterType == ChatFilterType.REGEX) Pattern.compile(pattern)
            patterns = patterns + ChatPattern(pattern, filterType)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removePattern(index: Int): Boolean {
        if (index < 0 || index >= patterns.size) return false
        patterns = patterns.filterIndexed { i, _ -> i != index }
        return true
    }

    fun clearAllPatterns() {
        patterns = emptyList()
    }

    fun updatePattern(index: Int, newPattern: String, filterType: ChatFilterType): Boolean {
        if (index < 0 || index >= patterns.size || newPattern.isBlank()) return false
        return try {
            if (filterType == ChatFilterType.REGEX) Pattern.compile(newPattern)
            patterns = patterns.mapIndexed { i, pattern ->
                if (i == index) ChatPattern(newPattern, filterType) else pattern
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}