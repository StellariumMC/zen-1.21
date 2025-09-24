package meowing.zen.ui

import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.elements.Button
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Text
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.canvas.core.animations.*
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.rendering.NVGRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text as MinecraftText

class AnimationTestScreen : Screen(MinecraftText.literal("Animation Test GUI")) {
    private val rootContainer = Rectangle()
        .backgroundColor(0x80121212.toInt())
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .padding(40f)

    private var statusText: Text? = null

    override fun init() {
        super.init()
        setupUI()
    }

    override fun close() {
        super.close()
        AnimationManager.clear()
        rootContainer.destroy()
    }

    override fun tick() {
        super.tick()
        AnimationManager.update()
        statusText?.text("Active Animations: ${AnimationManager.activeCount}")
    }

    private fun setupUI() {
        Text("Canvas Animation System Test")
            .color(0xFFFFFFFF.toInt())
            .fontSize(24f)
            .shadow(true)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(rootContainer)

        statusText = Text("Active Animations: 0")
            .color(0xFF60A5FA.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(rootContainer)

        setupAnimationBoxes()
        setupControlButtons()
    }

    private fun setupAnimationBoxes() {
        val boxContainer = Rectangle()
            .backgroundColor(0x80202020.toInt())
            .borderRadius(8f)
            .borderColor(0xFF404040.toInt())
            .borderThickness(1f)
            .setSizing(100f, Size.ParentPerc, 300f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .padding(20f)
            .childOf(rootContainer)

        Text("Animation Targets")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(boxContainer)

        val animatedBox = Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .borderRadius(8f)
            .setSizing(60f, Size.Pixels, 60f, Size.Pixels)
            .setPositioning(50f, Pos.ParentPixels, 30f, Pos.ParentPixels)
            .fadeIn(300)
            .childOf(boxContainer)

        val fadeBox = Rectangle()
            .backgroundColor(0xFF10B981.toInt())
            .borderRadius(8f)
            .setSizing(60f, Size.Pixels, 60f, Size.Pixels)
            .setPositioning(150f, Pos.ParentPixels, 30f, Pos.ParentPixels)
            .childOf(boxContainer)

        val colorBox = Rectangle()
            .backgroundColor(0xFFEF4444.toInt())
            .borderRadius(8f)
            .setSizing(60f, Size.Pixels, 60f, Size.Pixels)
            .setPositioning(250f, Pos.ParentPixels, 30f, Pos.ParentPixels)
            .childOf(boxContainer)

        val slideBox = Rectangle()
            .backgroundColor(0xFFF59E0B.toInt())
            .borderRadius(8f)
            .setSizing(60f, Size.Pixels, 60f, Size.Pixels)
            .setPositioning(350f, Pos.ParentPixels, 30f, Pos.ParentPixels)
            .childOf(boxContainer)

        val scaleBox = Rectangle()
            .backgroundColor(0xFF8B5CF6.toInt())
            .borderRadius(8f)
            .setSizing(40f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(460f, Pos.ParentPixels, 40f, Pos.ParentPixels)
            .childOf(boxContainer)

        val animatedText = Text("Animated Text")
            .color(0xFFFFFFFF.toInt())
            .fontSize(16f)
            .setPositioning(50f, Pos.ParentPixels, 120f, Pos.ParentPixels)
            .childOf(boxContainer)

        Text("Position")
            .color(0xFF9CA3AF.toInt())
            .fontSize(12f)
            .setPositioning(50f, Pos.ParentPixels, 100f, Pos.ParentPixels)
            .childOf(boxContainer)

        Text("Fade")
            .color(0xFF9CA3AF.toInt())
            .fontSize(12f)
            .setPositioning(150f, Pos.ParentPixels, 100f, Pos.ParentPixels)
            .childOf(boxContainer)

        Text("Color")
            .color(0xFF9CA3AF.toInt())
            .fontSize(12f)
            .setPositioning(250f, Pos.ParentPixels, 100f, Pos.ParentPixels)
            .childOf(boxContainer)

        Text("Slide")
            .color(0xFF9CA3AF.toInt())
            .fontSize(12f)
            .setPositioning(350f, Pos.ParentPixels, 100f, Pos.ParentPixels)
            .childOf(boxContainer)

        Text("Scale")
            .color(0xFF9CA3AF.toInt())
            .fontSize(12f)
            .setPositioning(460f, Pos.ParentPixels, 100f, Pos.ParentPixels)
            .childOf(boxContainer)

        Text("Text Color")
            .color(0xFF9CA3AF.toInt())
            .fontSize(12f)
            .setPositioning(50f, Pos.ParentPixels, 140f, Pos.ParentPixels)
            .childOf(boxContainer)

        setupAnimationControls(animatedBox, fadeBox, colorBox, slideBox, scaleBox, animatedText)
    }

    private fun setupAnimationControls(
        animatedBox: Rectangle,
        fadeBox: Rectangle,
        colorBox: Rectangle,
        slideBox: Rectangle,
        scaleBox: Rectangle,
        animatedText: Text
    ) {
        val buttonContainer = Rectangle()
            .backgroundColor(0x80202020.toInt())
            .borderRadius(8f)
            .borderColor(0xFF404040.toInt())
            .borderThickness(1f)
            .setSizing(100f, Size.ParentPerc, 200f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .padding(20f)
            .childOf(rootContainer)

        Text("Animation Controls")
            .color(0xFFE5E7EB.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(buttonContainer)

        val leftColumn = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(48f, Size.ParentPerc, 100f, Size.Auto)
            .setPositioning(0f, Pos.ParentPixels, 25f, Pos.ParentPixels)
            .childOf(buttonContainer)

        val rightColumn = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(48f, Size.ParentPerc, 100f, Size.Auto)
            .setPositioning(4f, Pos.AfterSibling, 0f, Pos.MatchSibling)
            .childOf(buttonContainer)

        Button("Move Box")
            .backgroundColor(0xFF3B82F6.toInt())
            .hoverColors(bg = 0xFF2563EB.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .onClick { _, _, _ ->
                animatedBox.moveTo(
                    (50..400).random().toFloat(),
                    (30..120).random().toFloat(),
                    800,
                    EasingType.EASE_OUT
                )
                true
            }
            .childOf(leftColumn)

        Button("Fade Toggle")
            .backgroundColor(0xFF10B981.toInt())
            .hoverColors(bg = 0xFF059669.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                if (fadeBox.visible) {
                    fadeBox.fadeOut(500, EasingType.EASE_IN)
                } else {
                    fadeBox.fadeIn(500, EasingType.EASE_OUT)
                }
                true
            }
            .childOf(leftColumn)

        Button("Change Color")
            .backgroundColor(0xFFEF4444.toInt())
            .hoverColors(bg = 0xFFDC2626.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                val colors = arrayOf(
                    0xFFEF4444.toInt(),
                    0xFF3B82F6.toInt(),
                    0xFF10B981.toInt(),
                    0xFFF59E0B.toInt(),
                    0xFF8B5CF6.toInt()
                )
                colorBox.colorTo(colors.random(), 600, EasingType.LINEAR)
                true
            }
            .childOf(leftColumn)

        Button("Slide In")
            .backgroundColor(0xFFF59E0B.toInt())
            .hoverColors(bg = 0xFFD97706.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                slideBox.slideIn(-200f, 0f, 700, EasingType.EASE_OUT)
                true
            }
            .childOf(leftColumn)

        Button("Bounce Scale")
            .backgroundColor(0xFF8B5CF6.toInt())
            .hoverColors(bg = 0xFF7C3AED.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .onClick { _, _, _ ->
                scaleBox.bounceScale(1.5f, 400)
                true
            }
            .childOf(rightColumn)

        Button("Multi-Scale Test")
            .backgroundColor(0xFF7C2D12.toInt())
            .hoverColors(bg = 0xFF9A3412.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                repeat(5) {
                    scaleBox.bounceScale(1.3f, 300)
                }
                true
            }
            .childOf(rightColumn)

        Button("Text Color")
            .backgroundColor(0xFF6366F1.toInt())
            .hoverColors(bg = 0xFF4F46E5.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                val colors = arrayOf(
                    0xFFFFFFFF.toInt(),
                    0xFF3B82F6.toInt(),
                    0xFF10B981.toInt(),
                    0xFFEF4444.toInt(),
                    0xFFF59E0B.toInt()
                )
                animatedText.colorTo(colors.random(), 500, EasingType.LINEAR)
                true
            }
            .childOf(rightColumn)

        Button("All Easing")
            .backgroundColor(0xFF14B8A6.toInt())
            .hoverColors(bg = 0xFF0D9488.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                animatedBox.moveTo(
                    (50..400).random().toFloat(),
                    (30..120).random().toFloat(),
                    1000,
                    EasingType.EASE_IN
                )

                fadeBox.fadeOut(300, EasingType.EASE_OUT) {
                    fadeBox.fadeIn(300, EasingType.EASE_IN)
                }

                colorBox.colorTo(
                    arrayOf(0xFF3B82F6.toInt(), 0xFF10B981.toInt(), 0xFF8B5CF6.toInt()).random(),
                    800,
                    EasingType.LINEAR
                )

                slideBox.slideIn(-150f, 0f, 600, EasingType.EASE_OUT)
                scaleBox.bounceScale(1.8f, 500)
                true
            }
            .childOf(rightColumn)

        Button("Chain Test")
            .backgroundColor(0xFFDB2777.toInt())
            .hoverColors(bg = 0xFFC2185B.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                animatedBox
                    .moveTo(300f, 50f, 500)
                    .bounceScale(1.4f, 300)
                    .colorTo(0xFF8B5CF6.toInt(), 400)

                fadeBox
                    .scaleTo(80f, 80f, 300)
                    .colorTo(0xFFEF4444.toInt(), 400)
                    .fadeOut(200) { fadeBox.fadeIn(200) }

                animatedText.colorTo(0xFF10B981.toInt(), 300)
                true
            }
            .childOf(rightColumn)

        Button("Stop All")
            .backgroundColor(0xFF6B7280.toInt())
            .hoverColors(bg = 0xFF4B5563.toInt())
            .textColor(0xFFE5E7EB.toInt())
            .borderRadius(6f)
            .padding(8f, 16f, 8f, 16f)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .onClick { _, _, _ ->
                AnimationManager.clear()
                true
            }
            .childOf(rightColumn)
    }

    private fun setupControlButtons() {
    }

    override fun render(drawContext: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())

        NVGRenderer.push()
        rootContainer.render(mouseX.toFloat(), mouseY.toFloat())
        NVGRenderer.pop()

        NVGRenderer.endFrame()
    }

    override fun shouldPause(): Boolean = false
}

@Zen.Command
object AnimationTestCommand : CommandUtils("animtest") {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        TickUtils.schedule(2) {
            mc.setScreen(AnimationTestScreen())
        }
        return 1
    }
}