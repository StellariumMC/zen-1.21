package xyz.meowing.zen.features.slayers.carrying

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.zen.events.core.GuiEvent

object CarryHUD {
    private data class Button(val x: Float, val y: Float, val width: Float, val height: Float, val action: String, val carryee: CarryCounter.Carryee, val tooltip: String)
    private data class RenderItem(val text: String, val x: Float, val y: Float, val color: Int, val shadow: Boolean)

    private val buttons = mutableListOf<Button>()
    private val renderItems = mutableListOf<RenderItem>()
    private var hoveredButton: Button? = null
    private var isRegistered = false
    private var guiClickHandler: EventCall? = null
    private var guiDrawHandler: EventCall? = null
    private const val NAME = "CarryHud"

    fun initialize() {
        HUDManager.register(NAME, "$prefix §f§lCarries:\n§7> §bPlayer1§f: §b5§f/§b10 §7(2.3s | 45/hr)\n§7> §bPlayer2§f: §b1§f/§b3 §7(15.7s | 32/hr)")
    }

    fun renderHUD(context: DrawContext) {
        if (CarryCounter.carries.isEmpty() || Zen.isInInventory || !HUDManager.isEnabled(NAME)) return

        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        buildRenderData()
        val lines = getLines()
        if (lines.isNotEmpty()) {
            var currentY = y
            val lineHeight = (client.textRenderer.fontHeight + 2) * scale
            for (line in lines) {
                Render2D.renderString(context, line, x, currentY, scale)
                currentY += lineHeight
            }
        }
    }

    private fun getLines(): List<String> {
        if (CarryCounter.carries.isEmpty() || Zen.isInInventory) return emptyList()

        val lines = mutableListOf<String>()
        lines.add("$prefix §f§lCarries:")
        CarryCounter.carries.mapTo(lines) {
            "§7> §b${it.name}§f: §b${it.count}§f/§b${it.total} §7(${it.getTimeSinceLastBoss()} | ${it.getBossPerHour()}§7)"
        }
        return lines
    }

    fun checkRegistration() {
        val shouldRegister = CarryCounter.carries.isNotEmpty()
        if (shouldRegister != isRegistered) {
            try {
                if (shouldRegister) {
                    guiClickHandler = EventBus.register<GuiEvent.Click> {
                        if (it.buttonState) onMouseInput()
                    }

                    guiDrawHandler = EventBus.register<GuiEvent.Render.Post> {
                        onGuiRender(it.context)
                    }
                } else {
                    guiClickHandler?.unregister()
                    guiDrawHandler?.unregister()
                }
                isRegistered = shouldRegister
            } catch (_: Exception) {
                isRegistered = false
            }
        }
    }

    private fun onGuiRender(context: DrawContext) {
        if (CarryCounter.carries.isEmpty() || !Zen.isInInventory || !HUDManager.isEnabled(NAME)) return
        buildRenderData()
        render(context)
    }

    private fun onMouseInput() {
        if (CarryCounter.carries.isEmpty() || !Zen.isInInventory) return

        val scale = HUDManager.getScale(NAME)
        buttons.find {
            KnitMouse.Scaled.x >= it.x && KnitMouse.Scaled.x <= it.x + it.width * scale && KnitMouse.Scaled.y >= it.y && KnitMouse.Scaled.y <= it.y + it.height * scale
        }?.let { button ->
            when (button.action) {
                "add" -> if (button.carryee.count < button.carryee.total) button.carryee.count++
                "subtract" -> if (button.carryee.count > 0) button.carryee.count--
                "remove" -> {
                    CarryCounter.removeCarryee(button.carryee.name)
                    checkRegistration()
                }
            }
        }
    }

    private fun buildRenderData() {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        renderItems.clear()
        buttons.clear()
        renderItems.add(RenderItem("$prefix §f§lCarries:", x, y, Colors.WHITE, true))

        CarryCounter.carries.forEachIndexed { i, carryee ->
            val lineY = y + (12f * scale) + i * (12f * scale)
            val str = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
            val textWidth = client.textRenderer.getWidth(str) * scale
            val btnX = x + textWidth + (4f * scale)

            renderItems.add(RenderItem(str, x, lineY, Colors.WHITE, true))

            listOf(
                "add" to "§a[+]",
                "subtract" to "§c[-]",
                "remove" to "§4[×]"
            ).forEachIndexed { j, (action, text) ->
                val buttonX = btnX + j * (20f * scale)
                buttons.add(Button(
                    buttonX, lineY, 18f, 10f, action, carryee,
                    when(action) {
                        "add" -> "§aIncrease"
                        "subtract" -> "§cDecrease"
                        else -> "§4Remove"
                    }
                ))
                renderItems.add(RenderItem(text, buttonX, lineY, 0xAAAAAA, false))
            }
        }
    }

    private fun render(context: DrawContext) {
        val scale = HUDManager.getScale(NAME)
        val mouseX = KnitMouse.Scaled.x
        val mouseY = KnitMouse.Scaled.y

        hoveredButton = buttons.find {
            mouseX >= it.x && mouseX <= it.x + it.width * scale && mouseY >= it.y && mouseY <= it.y + it.height * scale
        }

        renderItems.forEach { item ->
            val color = if (item.shadow || hoveredButton?.let { btn -> btn.x == item.x && btn.y == item.y } != true) item.color else Colors.WHITE
            Render2D.renderString(context, item.text, item.x, item.y, scale, color, if (item.shadow) Render2D.TextStyle.DROP_SHADOW else Render2D.TextStyle.DEFAULT)
        }

        renderTooltip(context, mouseX, mouseY)
    }

    private fun renderTooltip(context: DrawContext, mouseX: Double, mouseY: Double) {
        hoveredButton?.let { button ->
            val scale = HUDManager.getScale(NAME)
            val tooltipWidth = (client.textRenderer.getWidth(button.tooltip) + 8) * scale
            val tooltipHeight = 16 * scale
            val tooltipX = (mouseX - tooltipWidth / 2).coerceIn(2.0, (client.window.scaledWidth - tooltipWidth - 2).toDouble()).toInt()
            val tooltipY = (mouseY - tooltipHeight - 8 * scale).coerceAtLeast(2.0).toInt()

            context.fill(tooltipX, tooltipY, (tooltipX + tooltipWidth).toInt(), (tooltipY + tooltipHeight).toInt(), 0xC8000000.toInt())
            Render2D.renderString(context, button.tooltip, tooltipX + 4 * scale, tooltipY + 4 * scale, scale)
        }
    }
}