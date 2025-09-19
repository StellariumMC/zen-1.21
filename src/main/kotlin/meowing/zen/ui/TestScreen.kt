package meowing.zen.ui

import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.components.Button
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Text
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.rendering.NVGRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text as MinecraftText

class ButtonTestScreen : Screen(MinecraftText.literal("Button Test GUI")) {
    private val rootContainer = Rectangle()
        .backgroundColor(0x80121212.toInt())
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .padding(40f)

    private var clickCount = 0

    override fun init() {
        super.init()
        setupUI()
    }

    override fun close() {
        super.close()
        rootContainer.destroy()
    }

    private fun setupUI() {
        Text("Button Component Test Suite")
            .color(0xFFFFFFFF.toInt())
            .fontSize(24f)
            .shadow(true)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(rootContainer)

        Text("Click the buttons to test functionality")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, -20f, Pos.AfterSibling)
            .childOf(rootContainer)

        val leftColumn = Rectangle()
            .backgroundColor(0x80202020.toInt())
            .borderRadius(8f)
            .borderColor(0xFF404040.toInt())
            .borderThickness(1f)
            .setSizing(45f, Size.ParentPerc, 100f, Size.Auto)
            .setPositioning(10f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .padding(20f)
            .childOf(rootContainer)

        val rightColumn = Rectangle()
            .backgroundColor(0x80202020.toInt())
            .borderRadius(8f)
            .borderColor(0xFF404040.toInt())
            .borderThickness(1f)
            .setSizing(45f, Size.ParentPerc, 100f, Size.Auto)
            .setPositioning(10f, Pos.AfterSibling, 0f, Pos.MatchSibling)
            .padding(20f)
            .childOf(rootContainer)

        setupLeftColumn(leftColumn)
        setupRightColumn(rightColumn)
    }

    private fun setupLeftColumn(container: Rectangle) {
        Text("Primary Buttons")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(container)

        Button("Primary Action")
            .backgroundColor(0xFF3B82F6.toInt())
            .hoverColors(bg = 0xFF2563EB.toInt(), text = 0xFF0000FF.toInt())
            .pressedColors(bg = 0xFF1D4ED8.toInt(), text = 0xFFFF00FF.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(12f, 24f, 12f, 24f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                clickCount++
                println("Primary button clicked! Count: $clickCount")
                true
            }
            .childOf(container)

        Button("Success")
            .backgroundColor(0xFF10B981.toInt())
            .hoverColors(bg = 0xFF059669.toInt())
            .pressedColors(bg = 0xFF047857.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(10f, 20f, 10f, 20f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Success button clicked!")
                true
            }
            .childOf(container)

        Button("Danger")
            .backgroundColor(0xFFEF4444.toInt())
            .hoverColors(bg = 0xFFDC2626.toInt())
            .pressedColors(bg = 0xFFB91C1C.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(10f, 20f, 10f, 20f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Danger button clicked!")
                true
            }
            .childOf(container)

        Button("Warning")
            .backgroundColor(0xFFF59E0B.toInt())
            .hoverColors(bg = 0xFFD97706.toInt())
            .pressedColors(bg = 0xFFB45309.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(10f, 20f, 10f, 20f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Warning button clicked!")
                true
            }
            .childOf(container)
    }

    private fun setupRightColumn(container: Rectangle) {
        Text("Secondary & Special")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(container)

        Button("Secondary")
            .backgroundColor(0x00000000)
            .borderColor(0xFF6B7280.toInt())
            .borderThickness(1f)
            .hoverColors(bg = 0x1A6B7280)
            .pressedColors(bg = 0x336B7280)
            .textColor(0xFFD1D5DB.toInt())
            .borderRadius(6f)
            .padding(12f, 24f, 12f, 24f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Secondary button clicked!")
                true
            }
            .childOf(container)

        Button("Ghost Button")
            .backgroundColor(0x00000000)
            .borderColor(0x00000000)
            .borderThickness(0f)
            .hoverColors(bg = 0x1AFFFFFF)
            .pressedColors(bg = 0x33FFFFFF)
            .textColor(0xFF9CA3AF.toInt())
            .hoverColors(text = 0xFFFFFFFF.toInt())
            .borderRadius(4f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Ghost button clicked!")
                true
            }
            .childOf(container)

        Button("With Shadow")
            .backgroundColor(0xFF8B5CF6.toInt())
            .hoverColors(bg = 0xFF7C3AED.toInt())
            .pressedColors(bg = 0xFF6D28D9.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(8f)
            .shadow(true)
            .padding(12f, 24f, 12f, 24f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Shadow button clicked!")
                true
            }
            .childOf(container)

        Button("Disabled")
            .backgroundColor(0x804B5563.toInt())
            .textColor(0xFF6B7280.toInt())
            .borderRadius(6f)
            .padding(12f, 24f, 12f, 24f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ -> false }
            .childOf(container)

        val counterText = Text("Clicks: $clickCount")
            .color(0xFF60A5FA.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(container)

        Button("Reset Counter")
            .backgroundColor(0xFF374151.toInt())
            .hoverColors(bg = 0xFF4B5563.toInt())
            .textColor(0xFFE5E7EB.toInt())
            .borderRadius(4f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                clickCount = 0
                counterText.text("Clicks: 0")
                println("Counter reset!")
                true
            }
            .childOf(container)
    }

    override fun render(drawContext: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        //#if MC >= 1.21.7
        //$$ drawContext?.matrices?.pushMatrix()
        //#else
        drawContext?.matrices?.push()
        //#endif
        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        NVGRenderer.push()
        rootContainer.render(mouseX.toFloat(), mouseY.toFloat())
        NVGRenderer.pop()
        NVGRenderer.endFrame()
        //#if MC >= 1.21.7
        //$$ drawContext?.matrices?.popMatrix()
        //#else
        drawContext?.matrices?.pop()
        //#endif
    }

    override fun shouldPause(): Boolean = false
}

@Zen.Command
object ButtonTestCommand : CommandUtils("buttontest") {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        TickUtils.schedule(2) {
            mc.setScreen(ButtonTestScreen())
        }
        return 1
    }
}