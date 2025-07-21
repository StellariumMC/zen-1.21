package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.utils.Utils.MouseX
import meowing.zen.utils.Utils.MouseY
import meowing.zen.hud.HUDManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors

object CarryHUD {
    private data class Button(val x: Float, val y: Float, val width: Float, val height: Float, val action: String, val carryee: CarryCounter.Carryee, val tooltip: String)
    private data class RenderItem(val text: String, val x: Float, val y: Float, val color: Int, val shadow: Boolean)

    private val buttons = mutableListOf<Button>()
    private val renderItems = mutableListOf<RenderItem>()
    private var hoveredButton: Button? = null
    private var isRegistered = false
    private var guiClickHandler: EventBus.EventCall? = null
    private var guiDrawHandler: EventBus.EventCall? = null
    private const val name = "CarryHud"

    fun initialize() {
        HUDManager.register(name, "$prefix §f§lCarries:\n§7> §bPlayer1§f: §b5§f/§b10 §7(2.3s | 45/hr)\n§7> §bPlayer2§f: §b1§f/§b3 §7(15.7s | 32/hr)")
    }

    fun renderHUD(context: DrawContext) {
        if (CarryCounter.carryees.isEmpty() || Zen.isInInventory || !HUDManager.isEnabled(name)) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)

        val lines = getLines()
        if (lines.isNotEmpty()) {
            var currentY = y
            for (line in lines) {
                context.drawText(mc.textRenderer, line, x.toInt(), currentY.toInt(), Colors.WHITE, false)
                currentY += mc.textRenderer.fontHeight + 2
            }
        }
    }

    private fun getLines(): List<String> {
        if (CarryCounter.carryees.isEmpty() || Zen.isInInventory) return emptyList()

        val lines = mutableListOf<String>()
        lines.add("$prefix §f§lCarries:")
        CarryCounter.carryees.mapTo(lines) {
            "§7> §b${it.name}§f: §b${it.count}§f/§b${it.total} §7(${it.getTimeSinceLastBoss()} | ${it.getBossPerHour()}§7)"
        }
        return lines
    }

    fun checkRegistration() {
        val shouldRegister = CarryCounter.carryees.isNotEmpty()
        if (shouldRegister != isRegistered) {
            try {
                if (shouldRegister) {
                    guiClickHandler = EventBus.register<GuiEvent.Click> ({
                        if (it.state) onMouseInput()
                    })
                    guiDrawHandler = EventBus.register<GuiEvent.AfterRender> ({ onGuiRender(it.context) })
                } else {
                    guiClickHandler?.unregister()
                    guiDrawHandler?.unregister()
                }
                isRegistered = shouldRegister
            } catch (e: Exception) {
                isRegistered = false
            }
        }
    }

    private fun onGuiRender(context: DrawContext) {
        if (CarryCounter.carryees.isEmpty() || !Zen.isInInventory || !HUDManager.isEnabled(name)) return
        buildRenderData()
        render(context)
    }

    private fun onMouseInput() {
        if (CarryCounter.carryees.isEmpty() || !Zen.isInInventory) return

        buttons.find {
            MouseX >= it.x && MouseX <= it.x + it.width && MouseY >= it.y && MouseY <= it.y + it.height
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
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)

        renderItems.clear()
        buttons.clear()
        renderItems.add(RenderItem("$prefix §f§lCarries:", x, y, Colors.WHITE, true))

        CarryCounter.carryees.forEachIndexed { i, carryee ->
            val lineY = y + 12f + i * 12
            val str = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
            val textWidth = mc.textRenderer.getWidth(str)
            val btnX = x + textWidth + 4

            renderItems.add(RenderItem(str, x, lineY, Colors.WHITE, true))

            listOf(
                "add" to "§a[+]",
                "subtract" to "§c[-]",
                "remove" to "§4[×]"
            ).forEachIndexed { j, (action, text) ->
                val buttonX = btnX + j * 20
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
        hoveredButton = buttons.find {
            MouseX >= it.x && MouseX <= it.x + it.width && MouseY >= it.y && MouseY <= it.y + it.height
        }

        renderItems.forEach { item ->
            val color = if (item.shadow || hoveredButton?.let { btn ->
                    btn.x == item.x && btn.y == item.y
                } != true) item.color else Colors.WHITE

            context.drawText(mc.textRenderer, item.text, item.x.toInt(), item.y.toInt(), color, item.shadow)
        }

        renderTooltip(context, MouseY, MouseY)
    }

    private fun renderTooltip(context: DrawContext, mouseX: Double, mouseY: Double) {
        hoveredButton?.let { button ->
            val tooltipWidth = mc.textRenderer.getWidth(button.tooltip) + 8
            val tooltipHeight = 16
            val tooltipX = (mouseX - tooltipWidth / 2).coerceIn(2.0, (mc.window.scaledWidth - tooltipWidth - 2).toDouble()).toInt()
            val tooltipY = (mouseY - tooltipHeight - 8).coerceAtLeast(2.0).toInt()

            context.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xC8000000.toInt())
            context.drawText(mc.textRenderer, button.tooltip, tooltipX + 4, tooltipY + 4, Colors.WHITE, false)
        }
    }
}