package xyz.meowing.zen.hud

import xyz.meowing.zen.config.ConfigDelegate

class HUDElement(
    val id: String,
    var x: Float,
    var y: Float,
    var width: Int,
    var height: Int,
    var scale: Float = 1f,
    var text: String = "",
    configKey: String? = null
) {
    private val config = configKey ?: ""
    val enabled by ConfigDelegate<Boolean>(config)

    fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        return mouseX in x..(x + scaledWidth) && mouseY in y..(y + scaledHeight)
    }
}