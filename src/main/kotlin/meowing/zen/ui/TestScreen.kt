package meowing.zen.ui

import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.elements.Button
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Text
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.Manager
import meowing.zen.canvas.core.elements.CheckBox
import meowing.zen.canvas.core.elements.Switch
import meowing.zen.canvas.core.elements.Slider
import meowing.zen.canvas.core.components.SvgImage
import meowing.zen.canvas.core.elements.Keybind
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.rendering.NVGRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import java.awt.Color
import net.minecraft.text.Text as MinecraftText

class ButtonTestScreen : Screen(MinecraftText.literal("Button Test GUI")) {
    private val rootContainer = Rectangle()
        .backgroundColor(0x80121212.toInt())
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .padding(40f)

    private var clickCount = 0
    private var volumeValue = 0.7f
    private var brightnessValue = 0.5f
    private var sensitivityValue = 0.3f

    override fun init() {
        super.init()
        setupUI()
    }

    override fun close() {
        super.close()
        Manager.clear()
        rootContainer.destroy()
        NVGRenderer.cleanCache()
    }

    private fun setupUI() {
        rootContainer.children.clear()

        Text("Component Test Suite")
            .color(0xFFFFFFFF.toInt())
            .fontSize(24f)
            .shadow(true)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(rootContainer)

        Text("Test buttons, checkboxes, switches, and sliders")
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
        Text("Interactive Elements")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(container)

        SvgImage(svgPath = "/assets/zen/checkmark.svg", color = Color.RED)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(container)

        CheckBox()
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onValueChange { checked ->
                println("Checkbox is now: $checked")
            }
            .childOf(container)

        val switchContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 30f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        Text("Basic Switch")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(60f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .childOf(switchContainer)

        Switch()
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .onValueChange { enabled ->
                println("Basic switch is now: $enabled")
            }
            .childOf(switchContainer)

        val customSwitchContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 30f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(container)

        Text("Custom Switch")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(70f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .childOf(customSwitchContainer)

        Switch()
            .setSizing(60f, Size.Pixels, 30f, Size.Pixels)
            .trackEnabledColor(0xFF10B981.toInt())
            .trackDisabledColor(0xFF4B5563.toInt())
            .thumbColor(0xFFFFFFFF.toInt())
            .thumbDisabledColor(0xFF9CA3AF.toInt())
            .trackHoverColor(0xFF059669.toInt())
            .trackPressedColor(0xFF047857.toInt())
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .setEnabled(true, animated = false)
            .onValueChange { enabled ->
                println("Custom switch is now: $enabled")
            }
            .childOf(customSwitchContainer)

        val compactSwitchContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 25f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(container)

        Text("Compact Switch")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(50f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .childOf(compactSwitchContainer)

        Switch()
            .setSizing(40f, Size.Pixels, 30f, Size.Pixels)
            .trackEnabledColor(0xFFF59E0B.toInt())
            .trackDisabledColor(0xFF374151.toInt())
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
            .onValueChange { enabled ->
                println("Compact switch is now: $enabled")
            }
            .childOf(compactSwitchContainer)

        Text("Sliders")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .childOf(container)

        val volumeContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        val volumeLabel = Text("Volume: ${(volumeValue * 100).toInt()}%")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(volumeContainer)

        Slider()
            .setValue(volumeValue, animated = false)
            .setSizing(95f, Size.ParentPerc, 20f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .trackFillColor(0xFF4c87f9.toInt())
            .trackColor(0xFF424242.toInt())
            .thumbColor(0xFFFFFFFF.toInt())
            .thumbHoverColor(0xFFE5E7EB.toInt())
            .thumbPressedColor(0xFFD1D5DB.toInt())
            .onValueChange { value ->
                volumeValue = value as Float
                volumeLabel.text("Volume: ${(volumeValue * 100).toInt()}%")
                println("Volume changed to: ${(volumeValue * 100).toInt()}%")
            }
            .childOf(volumeContainer)

        val brightnessContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        val brightnessLabel = Text("Brightness: ${(brightnessValue * 100).toInt()}%")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(brightnessContainer)

        Slider()
            .setValue(brightnessValue, animated = false)
            .setSizing(95f, Size.ParentPerc, 20f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .trackFillColor(0xFFF59E0B.toInt())
            .trackColor(0xFF374151.toInt())
            .thumbColor(0xFFFFFFFF.toInt())
            .trackHoverColor(0xFF4B5563.toInt())
            .onValueChange { value ->
                brightnessValue = value as Float
                brightnessLabel.text("Brightness: ${(brightnessValue * 100).toInt()}%")
                println("Brightness changed to: ${(brightnessValue * 100).toInt()}%")
            }
            .childOf(brightnessContainer)

        val sensitivityContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        val sensitivityLabel = Text("Sensitivity: ${(sensitivityValue * 10).toInt()}")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(sensitivityContainer)

        Slider()
            .setValue(sensitivityValue, animated = false)
            .minValue(0f)
            .maxValue(1f)
            .step(0.1f)
            .setSizing(95f, Size.ParentPerc, 20f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .trackFillColor(0xFF10B981.toInt())
            .trackColor(0xFF4B5563.toInt())
            .thumbColor(0xFFFFFFFF.toInt())
            .onValueChange { value ->
                sensitivityValue = value as Float
                sensitivityLabel.text("Sensitivity: ${(sensitivityValue * 10).toInt()}")
                println("Sensitivity changed to: ${(sensitivityValue * 10).toInt()}")
            }
            .childOf(sensitivityContainer)

        Button("Primary Action")
            .backgroundColor(0xFF3B82F6.toInt())
            .hoverColors(bg = 0xFF2563EB.toInt(), text = 0xFF0000FF.toInt())
            .pressedColors(bg = 0xFF1D4ED8.toInt(), text = 0xFFFF00FF.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(12f, 24f, 12f, 24f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                clickCount++
                println("Primary button clicked! Count: $clickCount")
                true
            }
            .childOf(container)
            .addTooltip("This is a primary action button.")

        Keybind()
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
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

        Button("Disabled")
            .backgroundColor(0x804B5563.toInt())
            .textColor(0xFF6B7280.toInt())
            .borderRadius(6f)
            .padding(12f, 24f, 12f, 24f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ -> false }
            .childOf(container)

        Text("Advanced Sliders")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .childOf(container)

        val rangeContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(100f, Size.ParentPerc, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        val rangeLabel = Text("Range: 50")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(rangeContainer)

        Slider()
            .setValue(50f)
            .minValue(0f)
            .maxValue(100f)
            .step(1f)
            .setSizing(95f, Size.ParentPerc, 20f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .trackFillColor(0xFF8B5CF6.toInt())
            .trackColor(0xFF374151.toInt())
            .thumbColor(0xFFFFFFFF.toInt())
            .onValueChange { value ->
                rangeLabel.text("Range: ${(value as Float).toInt()}")
            }
            .childOf(rangeContainer)

        val compactContainer = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(60f, Size.ParentPerc, 35f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        Text("Compact")
            .color(0xFFD1D5DB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(compactContainer)

        Slider()
            .setValue(0.25f)
            .setSizing(100f, Size.ParentPerc, 15f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .trackFillColor(0xFFEF4444.toInt())
            .trackColor(0xFF6B7280.toInt())
            .thumbColor(0xFFFFFFFF.toInt())
            .childOf(compactContainer)

        val counterText = Text("Clicks: $clickCount")
            .color(0xFF60A5FA.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
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

        Button("Reset All Sliders")
            .backgroundColor(0xFF374151.toInt())
            .hoverColors(bg = 0xFF4B5563.toInt())
            .textColor(0xFFE5E7EB.toInt())
            .borderRadius(4f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                println("Reset all sliders!")
                true
            }
            .childOf(container)
    }

    override fun render(drawContext: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        NVGRenderer.push()
        rootContainer.render(mouseX.toFloat(), mouseY.toFloat())
        Manager.update()
        NVGRenderer.pop()
        NVGRenderer.endFrame()
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