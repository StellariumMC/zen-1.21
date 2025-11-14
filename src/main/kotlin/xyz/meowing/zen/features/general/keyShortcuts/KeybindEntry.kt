package xyz.meowing.zen.features.general.keyShortcuts

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class KeybindEntry(
    val keys: List<Int>,
    val command: String
) {
    companion object {
        val CODEC: Codec<KeybindEntry> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.listOf().fieldOf("keys").forGetter { it.keys },
                Codec.STRING.fieldOf("command").forGetter { it.command }
            ).apply(instance, ::KeybindEntry)
        }
    }
}