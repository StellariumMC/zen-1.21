package xyz.meowing.zen.hud

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.meowing.zen.api.data.StoredFile
import net.minecraft.client.gui.DrawContext

data class HUDPosition(var x: Float, var y: Float, var scale: Float = 1f, var enabled: Boolean = true) {
    companion object {
        val CODEC: Codec<HUDPosition> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.FLOAT.fieldOf("x").forGetter { it.x },
                Codec.FLOAT.fieldOf("y").forGetter { it.y },
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter { it.scale },
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter { it.enabled }
            ).apply(instance, ::HUDPosition)
        }
    }
}

object HUDManager {
    private val elements = mutableMapOf<String, String>()
    private val customRenderers = mutableMapOf<String, (DrawContext, Float, Float, Int, Int, Float, Float, Boolean) -> Unit>()
    private val customDimensions = mutableMapOf<String, Pair<Int, Int>>()

    private val hudData = StoredFile("hud_positions")
    private var positions: Map<String, HUDPosition> by hudData.map("positions", Codec.STRING, HUDPosition.CODEC, emptyMap())

    fun register(name: String, exampleText: String) {
        elements[name] = exampleText
    }

    fun registerCustom(
        name: String,
        width: Int,
        height: Int,
        customRenderer: (DrawContext, Float, Float, Int, Int, Float, Float, Boolean) -> Unit
    ) {
        elements[name] = ""
        customRenderers[name] = customRenderer
        customDimensions[name] = Pair(width, height)
    }

    fun getElements(): Map<String, String> = elements
    fun getCustomRenderer(name: String): ((DrawContext, Float, Float, Int, Int, Float, Float, Boolean) -> Unit)? = customRenderers[name]
    fun getCustomDimensions(name: String): Pair<Int, Int>? = customDimensions[name]

    fun getX(name: String): Float = positions[name]?.x ?: 50f
    fun getY(name: String): Float = positions[name]?.y ?: 50f
    fun getScale(name: String): Float = positions[name]?.scale ?: 1f
    fun isEnabled(name: String): Boolean = positions[name]?.enabled ?: true

    fun setPosition(name: String, x: Float, y: Float, scale: Float = 1f, enabled: Boolean = true) {
        positions = positions.toMutableMap().apply {
            this[name] = HUDPosition(x, y, scale, enabled)
        }
    }
}