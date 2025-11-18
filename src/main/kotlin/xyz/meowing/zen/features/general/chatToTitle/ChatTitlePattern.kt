package xyz.meowing.zen.features.general.chatToTitle

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder


data class ChatTitlePattern(
    val pattern: String,
    val title: String,
    val filterType: ChatToTitleType
) {
    fun matches(message: String): Boolean {
        return when (filterType) {
            ChatToTitleType.CONTAINS -> message.contains(pattern)
            ChatToTitleType.EQUALS -> message == pattern
            ChatToTitleType.REGEX -> try { message.matches(pattern.toRegex()) } catch (_: Exception) { false }
        }
    }

    companion object {
        val CODEC: Codec<ChatTitlePattern> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("pattern").forGetter { it.pattern },
                Codec.STRING.fieldOf("title").forGetter { it.title },
                Codec.STRING.xmap(
                    { ChatToTitleType.valueOf(it) },
                    { it.name }
                ).fieldOf("filterType").forGetter { it.filterType }
            ).apply(instance, ::ChatTitlePattern)
        }
    }
}