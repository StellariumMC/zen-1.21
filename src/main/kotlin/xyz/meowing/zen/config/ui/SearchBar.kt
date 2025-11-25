package xyz.meowing.zen.config.ui

import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.elements.TextInput
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.SvgImage
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.ui.Theme
import java.awt.Color

class SearchBar(
    private val onSearch: (String) -> Unit
) : VexelElement<SearchBar>() {
    var text: String = ""
        private set

    val input = TextInput(
        placeholder = "Search...",
        fontSize = 20f,
        backgroundColor = Theme.Bg.color,
        borderColor = Theme.Primary.color,
        hoverColor = null,
        pressedColor = Theme.BgDark.color,
        borderRadius = 8f,
        borderThickness = 2f,
        padding = floatArrayOf(8f, 12f, 8f, 12f)
    )
        .setSizing(310f, Size.Pixels, 40f, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
        .childOf(this)

    val buttonContainer = Rectangle(
        backgroundColor = Theme.Bg.color,
        borderColor = Theme.Primary.color,
        hoverColor = null,
        pressedColor = Theme.BgDark.color,
        borderRadius = 8f,
        borderThickness = 2f,
    )
        .setSizing(40f, Size.Pixels, 40f, Size.Pixels)
        .setPositioning(6f, Pos.AfterSibling, 0f, Pos.ParentPixels)
        .childOf(this)

    val editGuiLocationsButton = SvgImage("/assets/zen/editLocations.svg", color = Color.white)
        .setSizing(100f, Size.ParentPerc, 100, Size.ParentPerc)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
        .childOf(buttonContainer)

    init {
        setSizing(350f, Size.Pixels, 40f, Size.Pixels)
        setPositioning(0f, Pos.ScreenCenter, -50f, Pos.ScreenPixels)
        alignBottom()

        editGuiLocationsButton.onClick { _, _, _ ->
            TickScheduler.Client.post {
                client.setScreen(HUDEditor())
            }
            true
        }

        buttonContainer
            .onHover(
                { _, _ -> buttonContainer.colorTo(Theme.BgLight.color, 200, EasingType.EASE_IN) },
                { _, _ -> buttonContainer.colorTo(Theme.Bg.color, 200, EasingType.EASE_IN) }
            )

        input
            .onValueChange { newValue ->
                text = newValue as String
                onSearch(text)
            }
            .onHover(
                { _, _ -> input.background.colorTo(Theme.BgLight.color, 200, EasingType.EASE_IN) },
                { _, _ -> input.background.colorTo(Theme.Bg.color, 200, EasingType.EASE_IN) }
            )
            .background.dropShadow(10f, 3f, Theme.BgDark.color)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}