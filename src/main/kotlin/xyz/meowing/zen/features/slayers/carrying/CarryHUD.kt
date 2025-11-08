package xyz.meowing.zen.features.slayers.carrying

import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D

@Module
object CarryHUD {
    private const val NAME = "Carry Hud"

    private data class Button(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val action: () -> Any,
        val carry: CarryCounter.Carry,
        val tooltip: String
    )

    private val buttons = mutableListOf<Button>()

    private val x
        get() = HUDManager.getX(NAME)

    private val y
        get() = HUDManager.getY(NAME)

    private val scale
        get() = HUDManager.getScale(NAME)

    private val mouseX
        get() = KnitMouse.Scaled.x

    private val mouseY
        get() = KnitMouse.Scaled.y

    private var hoveredButton: Button? = null

    init {
        HUDManager.register(NAME, "$prefix §f§lCarries:\n§7> §bPlayer1§f: §b5§f/§b10 §7(2.3s | 45/hr)\n§7> §bPlayer2§f: §b1§f/§b3 §7(15.7s | 32/hr)")
    }

    /**
     * Using a method because CarryCounter calls it, to be replaced soon with a proper impl.
     */
    fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled(NAME)) return

        val lineHeight = (client.textRenderer.fontHeight + 2) * scale
        var currentY = y

        Render2D.renderString(context, "$prefix §f§lCarries:", x, currentY, scale)
        currentY += lineHeight

        CarryCounter.carries.forEach {
            val line = "§7> §b${it.name}§f: §b${it.count}§f/§b${it.total} §7(${it.getTimeSinceLastBoss()} | ${it.getBossPerHour()}§7)"
            Render2D.renderString(context, line, x, currentY, scale)
            currentY += lineHeight
        }
    }

    /**
     * Using a method because CarryCounter calls it, to be replaced soon with a proper impl.
     */
    fun renderInventoryHUD(context: DrawContext) {
        if (!HUDManager.isEnabled(NAME)) return

        buttons.clear()
        hoveredButton = null

        val lineHeight = (client.textRenderer.fontHeight + 2) * scale
        var currentY = y

        Render2D.renderString(context, "$prefix §f§lCarries:", x, currentY, scale)
        currentY += lineHeight

        CarryCounter.carries.forEach { carry ->
            val line = "§7> §b${carry.name}§f: §b${carry.count}§f/§b${carry.total} §7(${carry.getTimeSinceLastBoss()} | ${carry.getBossPerHour()}§7)"
            Render2D.renderString(context, line, x, currentY, scale)

            val textWidth = client.textRenderer.getWidth(line) * scale
            val btnX = x + textWidth + (4f * scale)

            listOf(
                Triple(
                    "§a[+]",
                    { if (carry.count < carry.total) carry.count++ },
                    "§aIncrease"
                ),
                Triple(
                    "§c[-]",
                    { if (carry.count > 0) carry.count-- },
                    "§cDecrease"
                ),
                Triple(
                    "§4[×]",
                    { CarryCounter.removeCarry(carry.name) },
                    "§4Remove"
                )
            ).forEachIndexed { j, (text, action, tooltip) ->
                val buttonX = btnX + j * (20f * scale)
                val isHovered = mouseX >= buttonX && mouseX <= buttonX + 18f * scale && mouseY >= currentY && mouseY <= currentY + 10f * scale

                if (isHovered) {
                    hoveredButton = Button(
                        buttonX,
                        currentY,
                        18f,
                        10f,
                        action,
                        carry,
                        tooltip
                    )
                }

                buttons.add(
                    Button(
                        buttonX,
                        currentY,
                        18f,
                        10f,
                        action,
                        carry,
                        tooltip
                    )
                )

                Render2D.renderString(
                    context, text, buttonX, currentY, scale,
                    if (isHovered) Colors.WHITE else 0xAAAAAA,
                    Render2D.TextStyle.DEFAULT
                )
            }

            currentY += lineHeight
        }

        renderTooltip(context)
    }

    /**
     * Using a method because CarryCounter calls it, to be replaced soon with a proper impl.
     */
    fun onMouseInput() {
        buttons.find {
            mouseX >= it.x && mouseX <= it.x + it.width * scale && mouseY >= it.y && mouseY <= it.y + it.height * scale
        }?.action
    }

    private fun renderTooltip(context: DrawContext) {
        hoveredButton?.let { button ->
            val tooltipWidth = (client.textRenderer.getWidth(button.tooltip) + 8) * scale
            val tooltipHeight = 16 * scale
            val tooltipX = (mouseX - tooltipWidth / 2).coerceIn(2.0, (client.window.scaledWidth - tooltipWidth - 2).toDouble()).toInt()
            val tooltipY = (mouseY - tooltipHeight - 8 * scale).coerceAtLeast(2.0).toInt()

            context.fill(tooltipX, tooltipY, (tooltipX + tooltipWidth).toInt(), (tooltipY + tooltipHeight).toInt(), 0xC8000000.toInt())
            Render2D.renderString(context, button.tooltip, tooltipX + 4 * scale, tooltipY + 4 * scale, scale)
        }
    }
}