package xyz.meowing.zen.features.general.chatCleaner

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class ChatPattern(
    val pattern: String,
    val filterType: ChatFilterType,
    val sendToGui: Boolean
) {
    fun matches(message: String): Boolean {
        return when (filterType) {
            ChatFilterType.CONTAINS -> message.contains(pattern)
            ChatFilterType.EQUALS -> message == pattern
            ChatFilterType.REGEX -> try { message.matches(pattern.toRegex()) } catch (_: Exception) { false }
        }
    }

    companion object {
        val CODEC: Codec<ChatPattern> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("pattern").forGetter { it.pattern },
                Codec.STRING.xmap(
                    { ChatFilterType.valueOf(it) },
                    { it.name }
                ).fieldOf("filterType").forGetter { it.filterType },
                Codec.BOOL.fieldOf("sendToGui").forGetter { it.sendToGui }
            ).apply(instance, ::ChatPattern)
        }
    }
}