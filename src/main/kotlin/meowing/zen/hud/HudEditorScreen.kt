package meowing.zen.hud

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper
import org.lwjgl.glfw.GLFW

class HudEditorScreen : Screen(Text.literal("HUD Editor")) {
    private var selectedElement: HudElement? = null
    private var dragOffset = Pair(0f, 0f)
    private var isDragging = false
    private var guiClickCall: EventBus.EventCall? = null
    private var guiKeyCall: EventBus.EventCall? = null

    override fun init() {
        HudManager.editMode = true
        guiClickCall = EventBus.register<GuiClickEvent> ({ event ->
            if (event.screen == this) handleClick(event)
        })
        guiKeyCall = EventBus.register<GuiKeyEvent> ({ event ->
            if (event.screen == this && event.key == GLFW.GLFW_KEY_ESCAPE) close()
        })
    }

    override fun close() {
        HudManager.editMode = false
        HudManager.saveConfig()
        guiClickCall?.unregister()
        guiKeyCall?.unregister()
        super.close()
    }

    private fun handleClick(event: GuiClickEvent) {
        if (event.state) {
            selectedElement = getElementAt(event.mx, event.my)?.also { element ->
                isDragging = true
                dragOffset = Pair(
                    event.mx.toFloat() - element.getActualX(width),
                    event.my.toFloat() - element.getActualY(height)
                )
            }
        } else isDragging = false
    }

    private fun getElementAt(x: Double, y: Double): HudElement? {
        return HudManager.getElements().lastOrNull { element ->
            val actualX = element.getActualX(width)
            val actualY = element.getActualY(height)
            val size = HudManager.getRenderer(element.id)?.getPreviewSize() ?: return@lastOrNull false
            x >= actualX && x <= actualX + size.first && y >= actualY && y <= actualY + size.second
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        renderUI(context)

        if (isDragging && selectedElement != null) updateDraggedElement(mouseX.toDouble(), mouseY.toDouble())

        renderHudElements(context)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun updateDraggedElement(mouseX: Double, mouseY: Double) {
        selectedElement?.let { element ->
            element.x = (mouseX - dragOffset.first).toFloat().coerceAtLeast(0f)
            element.y = (mouseY - dragOffset.second).toFloat().coerceAtLeast(0f)
        }
    }

    private fun renderHudElements(context: DrawContext) {
        println("Available elements: ${HudManager.getElements().map { it.id }}")
        HudManager.getElements().forEach { element ->
            println("Rendering element: ${element.id}, enabled: ${element.enabled}")
            if (!element.enabled) return@forEach
            val renderer = HudManager.getRenderer(element.id)
            println("Renderer for ${element.id}: $renderer")
            renderer?.render(context, mc.renderTickCounter)
        }
    }

    private fun renderUI(context: DrawContext) {
        context.fill(0, 0, width, 25, ColorHelper.getArgb(200, 0, 0, 0))
        context.drawText(textRenderer, "§cZen §7- §fHUD Editor", 10, 8, Colors.WHITE, false)

        selectedElement?.let { element ->
            val text = "§fSelected: §b${element.name}"
            val textWidth = textRenderer.getWidth(text)
            context.drawText(textRenderer, text, width - textWidth - 10, 8, Colors.WHITE, false)
        }
    }
}