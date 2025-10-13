package xyz.meowing.zen.ui

import xyz.meowing.zen.Zen
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.vexel.components.base.Offset
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.core.VexelScreen

class OffsetTestScreen : VexelScreen() {
    private val rootContainer = Rectangle()
        .backgroundColor(0x80121212.toInt())
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .padding(20f)
        .childOf(window)

    override fun afterInitialization() {
        setupUI()
    }

    private fun setupUI() {
        rootContainer.children.clear()

        Text("Offset & Positioning Test Suite")
            .color(0xFFFFFFFF.toInt())
            .fontSize(28f)
            .shadow(true)
            .setPositioning(0f, Pos.ParentCenter, 20f, Pos.ParentPixels)
            .childOf(rootContainer)

        val mainScrollArea = Rectangle()
            .backgroundColor(0x80202020.toInt())
            .borderRadius(12f)
            .borderColor(0xFF404040.toInt())
            .borderThickness(2f)
            .setSizing(90f, Size.ParentPerc, 85f, Size.ParentPerc)
            .setPositioning(0f, Pos.ParentCenter, 20f, Pos.AfterSibling)
            .padding(30f)
            .scrollable(true)
            .childOf(rootContainer)

        setupPixelOffsetTests(mainScrollArea)
        setupPercentOffsetTests(mainScrollArea)
        setupMixedOffsetTests(mainScrollArea)
        setupPaddingTests(mainScrollArea)
        setupNestedPaddingTests(mainScrollArea)
        setupPositioningValidationTests(mainScrollArea)
        setupContainerTests(mainScrollArea)
    }

    private fun setupPixelOffsetTests(container: Rectangle) {
        Text("Pixel Offset Tests")
            .color(0xFF60A5FA.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(container)

        Text("Base element (no offset)")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .setSizing(100f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        Text("With 20px X offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF10B981.toInt())
            .setSizing(100f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(20f, 0f)
            .childOf(container)

        Text("With 40px X offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFEF4444.toInt())
            .setSizing(100f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(40f, 0f)
            .childOf(container)

        Text("Negative 20px X offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(30f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFF59E0B.toInt())
            .setSizing(100f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(30f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(-20f, 0f)
            .childOf(container)

        Text("Y offset: 10px")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF8B5CF6.toInt())
            .setSizing(150f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(0f, 10f)
            .childOf(container)

        Text("XY offset: 30px, 15px")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFEC4899.toInt())
            .setSizing(120f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(30f, 15f)
            .childOf(container)
    }

    private fun setupPercentOffsetTests(container: Rectangle) {
        Text("Percent Offset Tests")
            .color(0xFF10B981.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 40f, Pos.AfterSibling)
            .childOf(container)

        Text("10% X offset from parent width")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .setSizing(150f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(10f, Offset.Percent, 0f, Offset.Pixels)
            .childOf(container)

        Text("20% X offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF10B981.toInt())
            .setSizing(150f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(20f, Offset.Percent, 0f, Offset.Pixels)
            .childOf(container)

        Text("5% Y offset from parent height")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFEF4444.toInt())
            .setSizing(200f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(0f, Offset.Pixels, 5f, Offset.Percent)
            .childOf(container)

        Text("15% X, 3% Y offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFF59E0B.toInt())
            .setSizing(180f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(15f, Offset.Percent, 3f, Offset.Percent)
            .childOf(container)
    }

    private fun setupMixedOffsetTests(container: Rectangle) {
        Text("Mixed Positioning + Offset Tests")
            .color(0xFFEF4444.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 40f, Pos.AfterSibling)
            .childOf(container)

        Text("ParentCenter + 50px X offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .setSizing(100f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentCenter, 10f, Pos.AfterSibling)
            .setOffset(50f, 0f)
            .childOf(container)

        Text("ParentCenter - 50px X offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF10B981.toInt())
            .setSizing(100f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentCenter, 10f, Pos.AfterSibling)
            .setOffset(-50f, 0f)
            .childOf(container)

        Text("AfterSibling + 20px spacing offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFEF4444.toInt())
            .setSizing(80f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFF59E0B.toInt())
            .setSizing(80f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.AfterSibling, 0f, Pos.MatchSibling)
            .setOffset(20f, 0f)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF8B5CF6.toInt())
            .setSizing(80f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.AfterSibling, 0f, Pos.MatchSibling)
            .setOffset(20f, 0f)
            .childOf(container)

        Text("ScreenCenter with percent offset")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFEC4899.toInt())
            .setSizing(120f, Size.Pixels, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ScreenCenter, 10f, Pos.AfterSibling)
            .setOffset(10f, Offset.Percent, 0f, Offset.Pixels)
            .childOf(container)
    }

    private fun setupPaddingTests(container: Rectangle) {
        Text("Padding Integration Tests")
            .color(0xFFF59E0B.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 40f, Pos.AfterSibling)
            .childOf(container)

        Text("Rectangle with 20px padding")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        val paddedRect = Rectangle()
            .backgroundColor(0x80374151.toInt())
            .borderColor(0xFF6B7280.toInt())
            .borderThickness(2f)
            .borderRadius(8f)
            .setSizing(100f, Size.ParentPerc, 150f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .padding(20f)
            .childOf(container)

        Text("Child 1 (ParentPixels)")
            .color(0xFFE5E7EB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(paddedRect)

        Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .setSizing(60f, Size.Pixels, 30f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(paddedRect)

        Text("Child 2 (AfterSibling)")
            .color(0xFFE5E7EB.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(paddedRect)

        Rectangle()
            .backgroundColor(0xFF10B981.toInt())
            .setSizing(60f, Size.Pixels, 30f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(paddedRect)

        Text("Variable padding test (top:30, right:10, bottom:30, left:10)")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .childOf(container)

        val variablePaddedRect = Rectangle()
            .backgroundColor(0x80451A03.toInt())
            .borderColor(0xFFF59E0B.toInt())
            .borderThickness(2f)
            .borderRadius(8f)
            .setSizing(100f, Size.ParentPerc, 120f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .padding(30f, 10f, 30f, 10f)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFF59E0B.toInt())
            .setSizing(100f, Size.ParentPerc, 20f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(variablePaddedRect)

        Rectangle()
            .backgroundColor(0xFFEF4444.toInt())
            .setSizing(100f, Size.ParentPerc, 20f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
            .childOf(variablePaddedRect)
    }

    private fun setupNestedPaddingTests(container: Rectangle) {
        Text("Nested Padding Tests")
            .color(0xFF8B5CF6.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 40f, Pos.AfterSibling)
            .childOf(container)

        Text("3 levels of nested padding")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        val level1 = Rectangle()
            .backgroundColor(0x80DC2626.toInt())
            .borderColor(0xFFEF4444.toInt())
            .borderThickness(2f)
            .borderRadius(8f)
            .setSizing(100f, Size.ParentPerc, 200f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .padding(15f)
            .childOf(container)

        Text("Level 1 (15px padding)")
            .color(0xFFFFFFFF.toInt())
            .fontSize(11f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(level1)

        val level2 = Rectangle()
            .backgroundColor(0x80D97706.toInt())
            .borderColor(0xFFF59E0B.toInt())
            .borderThickness(2f)
            .borderRadius(6f)
            .setSizing(100f, Size.ParentPerc, 140f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .padding(12f)
            .childOf(level1)

        Text("Level 2 (12px padding)")
            .color(0xFFFFFFFF.toInt())
            .fontSize(11f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(level2)

        val level3 = Rectangle()
            .backgroundColor(0x80059669.toInt())
            .borderColor(0xFF10B981.toInt())
            .borderThickness(2f)
            .borderRadius(4f)
            .setSizing(100f, Size.ParentPerc, 80f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .padding(10f)
            .childOf(level2)

        Text("Level 3 (10px padding)")
            .color(0xFFFFFFFF.toInt())
            .fontSize(11f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(level3)

        Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .borderRadius(3f)
            .setSizing(100f, Size.ParentPerc, 30f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(level3)
    }

    private fun setupPositioningValidationTests(container: Rectangle) {
        Text("Positioning Validation Tests")
            .color(0xFFEC4899.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 40f, Pos.AfterSibling)
            .childOf(container)

        Text("Valid: ParentPixels with constraint")
            .color(0xFF10B981.toInt())
            .fontSize(14f)
            .setPositioning(50f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        Text("Valid: ParentPercent with constraint")
            .color(0xFF10B981.toInt())
            .fontSize(14f)
            .setPositioning(50f, Pos.ParentPercent, 10f, Pos.AfterSibling)
            .childOf(container)

        Text("Valid: ScreenCenter with constraint")
            .color(0xFF10B981.toInt())
            .fontSize(14f)
            .setPositioning(100f, Pos.ScreenCenter, 10f, Pos.AfterSibling)
            .childOf(container)

        Text("AfterSibling doesn't need constraint")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF3B82F6.toInt())
            .setSizing(80f, Size.Pixels, 30f, Size.Pixels)
            .setPositioning(Pos.ParentPixels, Pos.AfterSibling)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFF10B981.toInt())
            .setSizing(80f, Size.Pixels, 30f, Size.Pixels)
            .setPositioning(Pos.AfterSibling, Pos.MatchSibling)
            .setOffset(10f, 0f)
            .childOf(container)

        Rectangle()
            .backgroundColor(0xFFEF4444.toInt())
            .setSizing(80f, Size.Pixels, 30f, Size.Pixels)
            .setPositioning(Pos.AfterSibling, Pos.MatchSibling)
            .setOffset(10f, 0f)
            .childOf(container)
    }

    private fun setupContainerTests(container: Rectangle) {
        Text("Container (vs Rectangle) Tests")
            .color(0xFF06B6D4.toInt())
            .fontSize(22f)
            .setPositioning(0f, Pos.ParentPixels, 40f, Pos.AfterSibling)
            .childOf(container)

        Text("Container with padding + scrollable")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
            .childOf(container)

        val testContainer = Container(
            padding = floatArrayOf(15f, 15f, 15f, 15f),
            scrollable = true,
            widthType = Size.ParentPerc,
            heightType = Size.Pixels
        )
            .setSizing(100f, Size.ParentPerc, 200f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .childOf(container)

        repeat(15) { i ->
            Rectangle()
                .backgroundColor(if (i % 2 == 0) 0xFF3B82F6.toInt() else 0xFF10B981.toInt())
                .borderRadius(4f)
                .setSizing(100f, Size.ParentPerc, 35f, Size.Pixels)
                .setPositioning(0f, Pos.ParentPixels, if (i == 0) 0f else 8f, if (i == 0) Pos.ParentPixels else Pos.AfterSibling)
                .childOf(testContainer)
        }

        Text("Container respects parent padding")
            .color(0xFF9CA3AF.toInt())
            .fontSize(14f)
            .setPositioning(0f, Pos.ParentPixels, 20f, Pos.AfterSibling)
            .childOf(container)

        val containerWithOffset = Container(
            padding = floatArrayOf(10f, 10f, 10f, 10f),
            widthType = Size.ParentPerc,
            heightType = Size.Pixels
        )
            .setSizing(80f, Size.ParentPerc, 100f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
            .setOffset(20f, 0f)
            .childOf(container)

        Text("Offset Container")
            .color(0xFFFFFFFF.toInt())
            .fontSize(12f)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .childOf(containerWithOffset)

        Rectangle()
            .backgroundColor(0xFFF59E0B.toInt())
            .borderRadius(4f)
            .setSizing(100f, Size.ParentPerc, 40f, Size.Pixels)
            .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
            .childOf(containerWithOffset)

        Text("End of Offset Tests")
            .color(0xFF9CA3AF.toInt())
            .fontSize(16f)
            .setPositioning(0f, Pos.ParentCenter, 40f, Pos.AfterSibling)
            .childOf(container)
    }
}

@Zen.Command
object OffsetTestCommand : Commodore("offsettest") {
    init {
        runs {
            OffsetTestScreen().display()
        }
    }
}