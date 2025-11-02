package xyz.meowing.zen.config.ui.panels

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateSize
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.SvgImage
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.zen.ui.Theme
import xyz.meowing.zen.config.ui.elements.base.ElementRenderer
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.config.FeatureElement
import java.awt.Color
import kotlin.math.absoluteValue

class SectionButton(
    val feature: FeatureElement,
    container: VexelElement<*>,
    private val onConfigUpdate: (String, Any) -> Unit
) : VexelElement<SectionButton>() {

    private var extended = false
    private var isAnimating = false
    private var isEnabled = false
    private val elementRenderers = mutableListOf<ElementRenderer>()

    private val buttonBackground = Rectangle(Theme.BgLight.color, 0x00000000, 0f, 0f)
        .setSizing(Panel.WIDTH, Size.Pixels, HEIGHT, Size.Pixels)
        .childOf(this)

    val titleText = Text(feature.featureName, Theme.Text.color, 16f)
        .setPositioning(4f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .childOf(buttonBackground)

    private val chevron = SvgImage(
        svgPath = "/assets/vexel/dropdown.svg",
        color = Color(Theme.TextMuted.color)
    )
        .setSizing(16f, Size.Pixels, 16f, Size.Pixels)
        .setPositioning(-4f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .alignRight()
        .childOf(buttonBackground)

    private val optionsContainer = Rectangle(0x00000000, 0x00000000, 0f, 0f)
        .setSizing(Panel.WIDTH, Size.Pixels, 0f, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, HEIGHT, Pos.ParentPixels)
        .childOf(this)

    companion object {
        const val HEIGHT = 32f
    }

    init {
        setSizing(Panel.WIDTH, Size.Pixels, HEIGHT, Size.Pixels)
        setPositioning(0f, Pos.ParentPixels, 0f, Pos.AfterSibling)
        childOf(container)

        chevron.visible = feature.options.values.flatten().isNotEmpty()

        isEnabled = ConfigManager.getConfigValue(feature.configElement.configKey) as? Boolean ?: false
        val targetColor = if (isEnabled) Theme.Primary.color else Theme.Bg.color
        buttonBackground.backgroundColor = targetColor

        buttonBackground.onHover(
            { _, _ ->
                val hoverColor = if (isEnabled) {
                    Color(68, 120, 175, 255).rgb
                } else {
                    Color(Theme.Bg.color).brighter().rgb
                }
                buttonBackground.colorTo(hoverColor, 150, EasingType.EASE_OUT)
            },
            { _, _ ->
                val baseColor = if (isEnabled) Theme.Primary.color else Theme.Bg.color
                buttonBackground.colorTo(baseColor, 150, EasingType.EASE_IN)
            }
        )

        buttonBackground.onClick { _, _, button ->
            when (button) {
                0 -> {
                    val pressColor = if (isEnabled) {
                        Color(Theme.Primary.color).darker().rgb
                    } else {
                        Color(Theme.Bg.color).darker().rgb
                    }
                    buttonBackground.colorTo(pressColor, 100, EasingType.EASE_OUT) {
                        val baseColor = if (isEnabled) Theme.Primary.color else Theme.Bg.color
                        buttonBackground.colorTo(baseColor, 100, EasingType.EASE_OUT)
                    }
                    toggleEnabled()
                    true
                }
                1 -> {
                    if (elementRenderers.isNotEmpty() && !isAnimating) {
                        toggleExpanded()
                    }
                    true
                }
                else -> false
            }
        }

        feature.options.values.flatten().forEach { option ->
            val renderer = ElementRenderer(option.configElement, onConfigUpdate)
            if (renderer.shouldShow()) {
                val element = renderer.createAndAttach(optionsContainer)
                element?.visible = false
                elementRenderers.add(renderer)
            }
        }

        optionsContainer.visible = false
    }

    private fun toggleEnabled() {
        isEnabled = !isEnabled
        onConfigUpdate(feature.configElement.configKey, isEnabled)
    }

    private fun toggleExpanded() {
        extended = !extended
        isAnimating = true

        val targetRotation = if (extended) -90f else 0f
        chevron.rotateTo(targetRotation, 200, EasingType.EASE_OUT)

        if (extended) {
            optionsContainer.visible = true
            elementRenderers.forEach { it.currentElement?.visible = true }

            val targetHeight = HEIGHT + calculateOptionsHeight()
            animateSize(Panel.WIDTH, targetHeight, 200, EasingType.EASE_OUT) {
                isAnimating = false
                parent?.let { p ->
                    if (p is VexelElement<*>) {
                        p.cache.invalidatePosition()
                        p.invalidateChildrenPositions()
                    }
                }
            }
        } else {
            animateSize(Panel.WIDTH, HEIGHT, 100, EasingType.EASE_IN) {
                optionsContainer.visible = false
                elementRenderers.forEach { it.currentElement?.visible = false }
                isAnimating = false
                parent?.let { p ->
                    if (p is VexelElement<*>) {
                        p.cache.invalidatePosition()
                        p.invalidateChildrenPositions()
                    }
                }
            }
        }
    }

    private fun calculateOptionsHeight(): Float {
        return elementRenderers.sumOf {
            it.currentElement?.height?.toDouble() ?: 0.0
        }.toFloat()
    }

    fun updateVisibility(panelExtended: Boolean) {
        visible = panelExtended
    }

    fun recalculateHeight() {
        if (!extended || isAnimating) return

        val newHeight = HEIGHT + calculateOptionsHeight()
        if ((newHeight - height).absoluteValue > 1f) {
            isAnimating = true
            animateSize(Panel.WIDTH, newHeight, 200, EasingType.EASE_OUT) {
                isAnimating = false
                parent?.let { p ->
                    if (p is VexelElement<*>) {
                        p.cache.invalidatePosition()
                        p.invalidateChildrenPositions()
                    }
                }
            }
        }
    }

    fun matchesSearch(query: String): Boolean {
        if (feature.featureName.contains(query, ignoreCase = true)) return true
        return feature.options.values.flatten().any {
            it.optionName.contains(query, ignoreCase = true)
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {}
}