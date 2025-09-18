package meowing.zen.canvas.core

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.KeyEvent
import meowing.zen.events.MouseEvent

@Zen.Module
object EventDispatcher {
    private val rootElements = mutableSetOf<CanvasElement<*>>()

    init {
        EventBus.register<MouseEvent.Click> { event ->
            if (handleMouseClick(mc.mouse.x.toFloat(), mc.mouse.y.toFloat(), event.button)) {
                event.cancel()
            }
        }

        EventBus.register<MouseEvent.Release> { event ->
            handleMouseRelease(mc.mouse.x.toFloat(), mc.mouse.y.toFloat(), event.button)
        }

        EventBus.register<MouseEvent.Move> {
            handleMouseMove(mc.mouse.x.toFloat(), mc.mouse.y.toFloat())
        }

        EventBus.register<MouseEvent.Scroll> { event ->
            (handleMouseScroll(mc.mouse.x.toFloat(), mc.mouse.y.toFloat(), event.horizontal, event.vertical))
        }

        EventBus.register<KeyEvent.Press> { event ->
            if (handleKeyPress(event.keyCode, event.scanCode, event.modifiers)) {
                event.cancel()
            }
        }

        EventBus.register<KeyEvent.Release> { event ->
            if (handleKeyRelease(event.keyCode, event.scanCode, event.modifiers)) {
                event.cancel()
            }
        }
    }

    fun registerRoot(element: CanvasElement<*>) {
        rootElements.add(element)
    }

    fun unregisterRoot(element: CanvasElement<*>) {
        rootElements.remove(element)
    }

    private fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return rootElements.any { it.handleMouseClick(mouseX, mouseY, button) }
    }

    private fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return rootElements.any { it.handleMouseRelease(mouseX, mouseY, button) }
    }

    private fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        return rootElements.any { it.handleMouseMove(mouseX, mouseY) }
    }

    private fun handleKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return rootElements.any { it.handleKeyPress(keyCode, scanCode, modifiers) }
    }

    private fun handleKeyRelease(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return rootElements.any { it.handleKeyRelease(keyCode, scanCode, modifiers) }
    }

    private fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        return rootElements.any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
    }
}