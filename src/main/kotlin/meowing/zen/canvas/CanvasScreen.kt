package meowing.zen.canvas

import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class CanvasScreen : Screen(Text.literal("Canvas Screen")) {
    val eventCalls = mutableListOf<EventBus.EventCall>()
    val window = CanvasWindow()

    final override fun init() {
        afterInitialization()

        eventCalls.add(EventBus.register<GuiEvent.Key> { event ->
            window.charType(event.key, event.scanCode, event.character)
        })

        super.init()
    }

    open fun afterInitialization() {}

    override fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        window.draw()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        window.mouseClick(button)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        window.mouseRelease(button)
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        window.mouseMove()
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        window.mouseScroll(horizontalAmount, verticalAmount)
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun close() {
        window.cleanup()
        eventCalls.clear()
        super.close()
    }

    override fun resize(client: net.minecraft.client.MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        window.onWindowResize()
    }
}