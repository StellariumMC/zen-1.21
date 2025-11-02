package xyz.meowing.zen.config.ui.elements.utils

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateColor
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.SvgImage
import xyz.meowing.vexel.components.core.Text
import java.awt.Color

fun <T : VexelElement<*>> T.fadeIn(
    duration: Long = 300,
    type: EasingType = EasingType.EASE_OUT,
    includeChildren: Boolean = true,
    onComplete: (() -> Unit)? = null
): T {
    visible = true

    if (includeChildren) {
        children.forEach { child ->
            when (child) {
                is Rectangle -> child.fadeIn(duration, type, includeChildren)
                is Text -> child.fadeIn(duration, type, includeChildren)
                is SvgImage -> child.fadeIn(duration, type, includeChildren)
            }
        }
    }

    when (this) {
        is Rectangle -> {
            val targetBg = (backgroundColor and 0x00FFFFFF) or (255 shl 24)
            val targetBorder = (borderColor and 0x00FFFFFF) or (255 shl 24)
            backgroundColor = backgroundColor and 0x00FFFFFF
            borderColor = borderColor and 0x00FFFFFF
            animateColor({ backgroundColor }, { backgroundColor = it }, targetBg, duration, type, onComplete)
            animateColor({ borderColor }, { borderColor = it }, targetBorder, duration, type)
        }
        is Text -> {
            val target = (textColor and 0x00FFFFFF) or (255 shl 24)
            textColor = textColor and 0x00FFFFFF
            animateColor({ textColor }, { textColor = it }, target, duration, type, onComplete)
        }
        is SvgImage -> {
            val target = (color.rgb and 0x00FFFFFF) or (255 shl 24)
            animateColor({ color.rgb }, { setSvgColor(Color(it, true)) }, target, duration, type, onComplete)
        }
    }

    return this
}

fun <T : VexelElement<*>> T.fadeOut(
    duration: Long = 300,
    type: EasingType = EasingType.EASE_IN,
    includeChildren: Boolean = true,
    onComplete: (() -> Unit)? = null
): T {
    if (includeChildren) {
        children.forEach { child ->
            when (child) {
                is Rectangle -> child.fadeOut(duration, type, includeChildren)
                is Text -> child.fadeOut(duration, type, includeChildren)
                is SvgImage -> child.fadeOut(duration, type, includeChildren)
            }
        }
    }

    when (this) {
        is Rectangle -> {
            val targetBg = backgroundColor and 0x00FFFFFF
            val targetBorder = borderColor and 0x00FFFFFF
            animateColor({ backgroundColor }, { backgroundColor = it }, targetBg, duration, type) {
                visible = false
                onComplete?.invoke()
            }
            animateColor({ borderColor }, { borderColor = it }, targetBorder, duration, type)
        }
        is Text -> {
            val target = textColor and 0x00FFFFFF
            animateColor({ textColor }, { textColor = it }, target, duration, type) {
                visible = false
                onComplete?.invoke()
            }
        }
        is SvgImage -> {
            val target = color.rgb and 0x00FFFFFF
            animateColor({ color.rgb }, { setSvgColor(Color(it, true)) }, target, duration, type) {
                visible = false
                onComplete?.invoke()
            }
        }
    }

    return this
}