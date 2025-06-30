package meowing.zen.hud

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.DataUtils
import meowing.zen.events.EventBus
import meowing.zen.events.HudRenderEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.util.concurrent.ConcurrentHashMap

object HudManager {
    private val elements = ConcurrentHashMap<String, HudElement>()
    private val renderers = ConcurrentHashMap<String, HudRenderer>()
    private val config = DataUtils("hud", HudConfig())
    private var hudRenderCall: EventBus.EventCall? = null
    var editMode = false

    init {
        hudRenderCall = EventBus.register<HudRenderEvent> ({ event ->
            if (!editMode && mc.currentScreen == null) renderHudElements(event.context)
        })
    }

    fun addElement(id: String, name: String, x: Float, y: Float, width: Float, height: Float, textProvider: (MinecraftClient) -> String) {
        val element = HudElement(x, y, width, height, 1.0f, true, id, name)
        elements[id] = element
        renderers[id] = TextHudRenderer(element) { textProvider(mc) }
        loadElementConfig(element)
    }

    fun addBoxElement(id: String, name: String, x: Float, y: Float, width: Float, height: Float) {
        val element = HudElement(x, y, width, height, 1.0f, true, id, name)
        elements[id] = element
        renderers[id] = BoxHudRenderer(element)
        loadElementConfig(element)
    }

    fun registerCustom(element: HudElement, renderer: HudRenderer) {
        elements[element.id] = element
        renderers[element.id] = renderer
        loadElementConfig(element)
    }

    private fun loadElementConfig(element: HudElement) {
        val elementData = config.getData().elements[element.id]
        if (elementData != null) {
            element.x = elementData.x
            element.y = elementData.y
            element.scale = elementData.scale
            element.enabled = elementData.enabled
        }
    }

    private fun renderHudElements(context: DrawContext) {
        context.matrices.push()
        context.matrices.translate(0f, 0f, 100f)
        elements.values.forEach { element ->
            if (element.enabled) renderers[element.id]?.render(context, mc.renderTickCounter)
        }
        context.matrices.pop()
    }

    fun saveConfig() {
        val hudConfig = config.getData()
        elements.values.forEach { element ->
            hudConfig.elements[element.id] = HudElementData(
                element.x,
                element.y,
                element.scale,
                element.enabled
            )
        }
        config.save()
    }

    fun getElements() = elements.values.toList()
    fun getRenderer(id: String) = renderers[id]
}