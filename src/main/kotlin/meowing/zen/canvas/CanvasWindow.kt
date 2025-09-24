package meowing.zen.canvas

import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.animations.AnimationManager
import meowing.zen.utils.MouseUtils
import meowing.zen.utils.rendering.NVGRenderer
import net.minecraft.client.util.Window

class CanvasWindow {
    val children: MutableList<CanvasElement<*>> = mutableListOf()
    val window: Window get() = mc.window

    fun addChild(element: CanvasElement<*>) {
        element.parent = this
        children.add(element)
    }

    fun removeChild(element: CanvasElement<*>) {
        element.parent = null
        children.remove(element)
    }

    fun draw() {
        NVGRenderer.beginFrame(window.width.toFloat(), window.height.toFloat())
        NVGRenderer.push()
        children.forEach { it.render(0f, 0f) }
        AnimationManager.update()
        NVGRenderer.pop()
        NVGRenderer.endFrame()
    }

    fun mouseClick(button: Int) {
        children.reversed().any { it.handleMouseClick(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat(), button) }
    }

    fun mouseRelease(button: Int) {
        children.reversed().forEach { it.handleMouseRelease(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat(), button) }
    }

    fun mouseMove() {
        children.reversed().any { it.handleMouseMove(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat()) }
    }

    fun mouseScroll(horizontalDelta: Double, verticalDelta: Double) {
        children.reversed().any { it.handleMouseScroll(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat(), horizontalDelta, verticalDelta) }
    }

    fun charType(keyCode: Int, scanCode: Int , charTyped: Char) {
        children.reversed().any { it.handleCharType(keyCode, scanCode, charTyped) }
    }

    fun onWindowResize() {
        children.forEach { it.onWindowResize() }
    }

    fun cleanup() {
        children.forEach { it.destroy() }
        children.clear()
        AnimationManager.clear()
        NVGRenderer.cleanCache()
    }
}