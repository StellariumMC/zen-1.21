package xyz.meowing.zen.hud

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.zen.api.data.StoredFile
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object HUDManager {
    val elements = mutableMapOf<String, HUDElement>()
    val customRenderers = mutableMapOf<String, (GuiGraphics) -> Unit>()
    val customSizes = mutableMapOf<String, Pair<Int, Int>>()

    data class HudLayoutData(
        var x: Float,
        var y: Float,
        var scale: Float = 1f
    ) {
        companion object {
            val CODEC: Codec<HudLayoutData> = Codec.FLOAT.listOf().comapFlatMap(
                { list ->
                    if (list.size == 3) {
                        DataResult.success(HudLayoutData(list[0], list[1], list[2]))
                    } else {
                        DataResult.error { "Invalid layout data size" }
                    }
                },
                { data -> listOf(data.x, data.y, data.scale) }
            )
        }
    }

    private val layoutStore = StoredFile("config/HUD")

    private var layouts by layoutStore.map(
        "layouts",
        Codec.STRING,
        HudLayoutData.CODEC,
        emptyMap()
    )


    fun register(id: String, text: String, configKey: String? = null) {
        elements[id] = HUDElement(id, 20f, 20f, 0, 0, text = text, configKey = configKey)
        loadLayout(id)
    }

    fun registerCustom(
        id: String,
        width: Int,
        height: Int,
        renderer: (GuiGraphics) -> Unit,
        configKey: String? =  null
    ) {
        customRenderers[id] = renderer
        customSizes[id] = width to height
        elements[id] = HUDElement(id, 20f, 20f, width, height, configKey = configKey)
        loadLayout(id)
    }

    fun saveAllLayouts() {
        layouts = elements.mapValues { (_, element) ->
            HudLayoutData(element.x, element.y, element.scale)
        }
        layoutStore.forceSave()
    }

    fun loadAllLayouts() { layouts.keys.forEach { loadLayout(it) } }

    fun loadLayout(id: String) {
        layouts.forEach { (id, layout) ->
            elements[id]?.apply {
                x = layout.x
                y = layout.y
                scale = layout.scale
            }
        }
    }

    fun getX(id: String): Float = elements[id]?.x ?: 0f
    fun getY(id: String): Float = elements[id]?.y ?: 0f
    fun getScale(id: String): Float = elements[id]?.scale ?: 1f
}
