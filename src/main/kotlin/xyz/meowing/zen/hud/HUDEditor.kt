package xyz.meowing.zen.hud

import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.knit.api.screen.KnitScreen
import xyz.meowing.zen.hud.HUDManager.customRenderers
import xyz.meowing.zen.utils.Render2D.height
import xyz.meowing.zen.utils.Render2D.width
import java.awt.Color

//#if MC >= 1.21.9
//$$ import xyz.meowing.zen.utils.Render2D.renderOutline
//#endif

class HUDEditor : KnitScreen("HUD Editor") {
    private val borderHoverColor = Color(255, 255, 255).rgb
    private val borderNormalColor = Color(100, 100, 120).rgb

    private var dragging: HUDElement? = null
    private var offsetX = 0f
    private var offsetY = 0f

    override fun onInitGui() {
        super.onInitGui()
        HUDManager.loadAllLayouts()
    }

    override fun onCloseGui() {
        super.onCloseGui()
        HUDManager.saveAllLayouts()
    }

    override fun onRender(context: GuiGraphics?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val context = context ?: return
        context.fill(0, 0, width, width, 0x90000000.toInt())

        HUDManager.elements.values.forEach { element ->
            if (!element.enabled) return@forEach

            //#if MC >= 1.21.7
            //$$ context.pose().pushMatrix()
            //$$ context.pose().translate(element.x, element.y)
            //$$ context.pose().scale(element.scale, element.scale)
            //#else
            context.pose().pushPose()
            context.pose().translate(element.x, element.y, 0f)
            context.pose().scale(element.scale, element.scale, 1f)
            //#endif

            val isHovered = element.isHovered(mouseX.toFloat(), mouseY.toFloat())

            val borderColor = if (isHovered) borderHoverColor else borderNormalColor

            val alpha = if (isHovered) 140 else 90
            val custom = customRenderers[element.id]

            if (custom != null) {
                context.renderOutline(0, 0, element.width, element.height, borderColor)
                context.fill(0,0, element.width, element.height, Color(30, 35, 45, alpha).rgb)
                custom(context)
            } else {
                if (element.width == 0 && element.height == 0) {
                    element.width = element.text.width()
                    element.height = element.text.height()
                }

                context.renderOutline(-2, -3, element.width + 2, element.height + 2, borderColor)
                context.fill(-2,-3, element.width + 2, element.height + 2, Color(30, 35, 45, alpha).rgb)

                val lines = element.text.split("\n")
                lines.forEachIndexed { index, line ->
                    val textY = (index * client.font.lineHeight)
                    context.drawString(client.font, line, 0, textY, 0xFFFFFF)
                }
            }

            //#if MC >= 1.21.7
            //$$ context.pose().popMatrix()
            //#else
            context.pose().popPose()
            //#endif
        }

        context.drawString(client.font, "Drag elements. Press ESC to exit.", 10, 10, 0xFFFFFF)
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        val hovered = HUDManager.elements.values.firstOrNull { it.isHovered(mouseX.toFloat(), mouseY.toFloat()) }
        if (hovered != null) {
            dragging = hovered
            offsetX = mouseX.toFloat() - hovered.x
            offsetY = mouseY.toFloat() - hovered.y
        }
    }

    override fun onMouseMove(mouseX: Int, mouseY: Int) {
        dragging?.let {
            it.x = KnitMouse.Scaled.x.toFloat() - offsetX
            it.y = KnitMouse.Scaled.y.toFloat() - offsetY
        }
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, button: Int) {
        dragging = null
    }

    override fun onMouseScroll(horizontal: Double, vertical: Double) {
        val hovered = HUDManager.elements.values.firstOrNull { it.isHovered(KnitMouse.Scaled.x.toFloat(), KnitMouse.Scaled.y.toFloat()) }

        if (hovered != null) {
            val scaleDelta = if (vertical > 0) 0.1f else -0.1f
            hovered.scale = (hovered.scale + scaleDelta).coerceIn(0.2f, 5.0f)
        }
    }

    override fun isPauseScreen(): Boolean = false
}