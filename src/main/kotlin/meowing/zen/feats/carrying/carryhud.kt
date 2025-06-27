package meowing.zen.feats.carrying

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.GuiClickEvent
import meowing.zen.events.GuiAfterRenderEvent
import meowing.zen.hud.HudElement
import meowing.zen.hud.HudManager
import meowing.zen.hud.HudRenderer
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer

object CarryHudState {
    var element: HudElement? = null
    var renderer: carryhud? = null

    fun init() {
        if (element != null) return
        element = HudElement(100f, 100f, 200f, 50f, 1.0f, true, "carry_hud", "Carry HUD")
        renderer = carryhud(element!!)
        HudManager.registerCustom(element!!, renderer!!)
        HudLayerRegistrationCallback.EVENT.register { registrar ->
            registrar.addLayer(object : IdentifiedLayer {
                override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
                    if (!HudManager.editMode && element!!.enabled) renderer!!.render(context, tickCounter)
                }
                override fun id(): Identifier? = Identifier.of("zen", "carry_hud")
            })
        }
    }
}

data class ButtonInfo(val tooltip: String, val x: Int, val y: Int, val width: Int, val height: Int)

class carryhud(element: HudElement) : HudRenderer(element) {
    private var hoveredButton: ButtonInfo? = null

    init {
        EventBus.register<GuiClickEvent> ({ event ->
            if (event.state) handleClick(event.mx, event.my)
        })
        EventBus.register<GuiAfterRenderEvent> ({ event ->
            if (event.screen is HandledScreen<*>) renderInventoryOverlay()
        })
        CarryHudState.element = element
        CarryHudState.renderer = this
    }

    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        if (!HudManager.editMode && carrycounter.carryees.isEmpty()) return
        if (HudManager.editMode && getDummyCarryees().isEmpty()) return

        val actualX = element.getActualX(mc.window.scaledWidth)
        val actualY = element.getActualY(mc.window.scaledHeight)

        context.matrices.push()
        context.matrices.translate(actualX.toDouble(), actualY.toDouble(), 0.0)
        context.matrices.scale(element.scale, element.scale, 1.0f)

        context.drawText(mc.textRenderer, "§c[Zen] §f§lCarries:", 0, 0, Colors.WHITE, false)

        if (HudManager.editMode) {
            getDummyCarryees().forEachIndexed { i, carryee ->
                val y = 12 + i * 12
                val text = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
                context.drawText(mc.textRenderer, text, 0, y, Colors.WHITE, false)
            }
        } else {
            carrycounter.carryees.forEachIndexed { i, carryee ->
                val y = 12 + i * 12
                val text = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
                context.drawText(mc.textRenderer, text, 0, y, Colors.WHITE, false)
            }
        }

        context.matrices.pop()
    }

    override fun getPreviewSize(): Pair<Float, Float> {
        val titleWidth = mc.textRenderer.getWidth("§c[Zen] §f§lCarries:")
        val maxWidth = if (HudManager.editMode) {
            getDummyCarryees().maxOfOrNull { carryee ->
                mc.textRenderer.getWidth("§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)")
            } ?: titleWidth
        } else {
            carrycounter.carryees.maxOfOrNull { carryee ->
                mc.textRenderer.getWidth("§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)")
            } ?: titleWidth
        }

        val size = if (HudManager.editMode) getDummyCarryees().size else carrycounter.carryees.size
        return Pair(
            maxOf(titleWidth, maxWidth) * element.scale,
            (12 + size * 12) * element.scale
        )
    }

    private fun getDummyCarryees() = listOf(
        DummyCarryee("Player1", 45, 50, "129s", "23/h"),
        DummyCarryee("Player2", 23, 30, "45s", "60/h"),
        DummyCarryee("Player3", 67, 100, "32s", "46/h")
    )

    fun renderInventoryOverlay() {
        if (!HudManager.editMode && carrycounter.carryees.isEmpty()) return
        if (HudManager.editMode && getDummyCarryees().isEmpty()) return

        val context = mc.currentScreen?.let {
            if (it is HandledScreen<*>) DrawContext(mc, mc.bufferBuilders.entityVertexConsumers) else null
        } ?: return

        val mouseX = mc.mouse.x.toInt() * mc.window.scaledWidth / mc.window.width
        val mouseY = mc.mouse.y.toInt() * mc.window.scaledHeight / mc.window.height
        val actualX = element.getActualX(mc.window.scaledWidth)
        val actualY = element.getActualY(mc.window.scaledHeight)
        hoveredButton = null

        context.matrices.push()
        context.matrices.translate(actualX.toDouble(), actualY.toDouble(), 0.0)
        context.matrices.scale(element.scale, element.scale, 1.0f)

        context.drawText(mc.textRenderer, "§c[Zen] §f§lCarries:", 0, 0, Colors.WHITE, true)

        if (HudManager.editMode) {
            getDummyCarryees().forEachIndexed { i, carryee ->
                val y = 12 + i * 12
                val text = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
                val x = mc.textRenderer.getWidth(text) + 4

                context.drawText(mc.textRenderer, text, 0, y, Colors.WHITE, true)

                val buttons = listOf(
                    Triple(x, "§a[+]", "§aIncrease"),
                    Triple(x + 20, "§c[-]", "§cDecrease"),
                    Triple(x + 40, "§4[×]", "§4Remove")
                )

                buttons.forEach { (btnX, btnText, tooltip) ->
                    context.drawText(mc.textRenderer, btnText, btnX, y, Colors.WHITE, true)
                    if (isMouseOver(mouseX, mouseY, actualX, actualY, btnX, y, 18, 10))
                        hoveredButton = ButtonInfo(tooltip, btnX, y, 18, 10)
                }
            }
        } else {
            carrycounter.carryees.forEachIndexed { i, carryee ->
                val y = 12 + i * 12
                val text = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
                val x = mc.textRenderer.getWidth(text) + 4

                context.drawText(mc.textRenderer, text, 0, y, Colors.WHITE, true)

                val buttons = listOf(
                    Triple(x, "§a[+]", "§aIncrease"),
                    Triple(x + 20, "§c[-]", "§cDecrease"),
                    Triple(x + 40, "§4[×]", "§4Remove")
                )

                buttons.forEach { (btnX, btnText, tooltip) ->
                    context.drawText(mc.textRenderer, btnText, btnX, y, Colors.WHITE, true)
                    if (isMouseOver(mouseX, mouseY, actualX, actualY, btnX, y, 18, 10))
                        hoveredButton = ButtonInfo(tooltip, btnX, y, 18, 10)
                }
            }
        }

        context.matrices.pop()
        renderTooltip(context, mouseX, mouseY)
    }

    private fun renderTooltip(context: DrawContext, mouseX: Int, mouseY: Int) {
        hoveredButton?.let { button ->
            val tooltipWidth = mc.textRenderer.getWidth(button.tooltip) + 8
            val tooltipHeight = 16
            val tooltipX = (mouseX - tooltipWidth / 2).coerceIn(2, mc.window.scaledWidth - tooltipWidth - 2)
            val tooltipY = (mouseY - tooltipHeight - 8).coerceAtLeast(2)

            context.matrices.push()
            context.matrices.translate(0.0, 0.0, 300.0)

            context.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight,
                ColorHelper.getArgb(200, 0, 0, 0))

            val borderColor = ColorHelper.getArgb(255, 100, 100, 100)
            listOf(
                Pair(tooltipX - 1 to tooltipY - 1, (tooltipX + tooltipWidth + 1) to tooltipY),
                Pair(tooltipX - 1 to (tooltipY + tooltipHeight), (tooltipX + tooltipWidth + 1) to (tooltipY + tooltipHeight + 1)),
                Pair(tooltipX - 1 to tooltipY, tooltipX to (tooltipY + tooltipHeight)),
                Pair((tooltipX + tooltipWidth) to tooltipY, (tooltipX + tooltipWidth + 1) to (tooltipY + tooltipHeight))
            ).forEach { (start, end) ->
                context.fill(start.first, start.second, end.first, end.second, borderColor)
            }

            context.drawText(mc.textRenderer, button.tooltip, tooltipX + 4, tooltipY + 4, Colors.WHITE, false)
            context.matrices.pop()
        }
    }

    private fun isMouseOver(mouseX: Int, mouseY: Int, actualX: Float, actualY: Float, btnX: Int, btnY: Int, width: Int, height: Int): Boolean {
        return mouseX >= actualX + btnX * element.scale && mouseX <= actualX + (btnX + width) * element.scale && mouseY >= actualY + btnY * element.scale && mouseY <= actualY + (btnY + height) * element.scale
    }

    fun handleClick(mouseX: Double, mouseY: Double): Boolean {
        if (!HudManager.editMode && carrycounter.carryees.isEmpty()) return false
        if (HudManager.editMode) return false
        if (mc.currentScreen !is HandledScreen<*>) return false

        val actualX = element.getActualX(mc.window.scaledWidth)
        val actualY = element.getActualY(mc.window.scaledHeight)
        val scaledMouseX = ((mouseX - actualX) / element.scale).toInt()
        val scaledMouseY = ((mouseY - actualY) / element.scale).toInt()

        return carrycounter.carryees.withIndex().any { (i, carryee) ->
            val y = 12 + i * 12
            val text = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
            val x = mc.textRenderer.getWidth(text) + 4

            when {
                scaledMouseX in x..(x + 18) && scaledMouseY in y..(y + 10) -> {
                    if (carryee.count < carryee.total) carryee.count++
                    true
                }
                scaledMouseX in (x + 20)..(x + 38) && scaledMouseY in y..(y + 10) -> {
                    if (carryee.count > 0) carryee.count--
                    true
                }
                scaledMouseX in (x + 40)..(x + 58) && scaledMouseY in y..(y + 10) -> {
                    carrycounter.removeCarryee(carryee.name)
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        fun initialize() = CarryHudState.init()
    }
}

data class DummyCarryee(
    val name: String,
    var count: Int,
    val total: Int,
    private val timeSince: String,
    private val bossPerHour: String
) {
    fun getTimeSinceLastBoss() = timeSince
    fun getBossPerHour() = bossPerHour
}