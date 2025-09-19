package meowing.zen.canvas.core.animations

import meowing.zen.canvas.core.CanvasElement

private val elementOriginalSizes = mutableMapOf<String, Pair<Float, Float>>()
private val elementOriginalPositions = mutableMapOf<String, Pair<Float, Float>>()

fun <T : CanvasElement<T>> T.fadeIn(
    duration: Long = 300,
    type: EasingType = EasingType.EASE_OUT,
    onComplete: (() -> Unit)? = null
): Animation<*> {
    visible = true

    return when (this) {
        is meowing.zen.canvas.core.components.Rectangle -> {
            val targetColor = (backgroundColor and 0x00FFFFFF) or (255 shl 24)
            backgroundColor = backgroundColor and 0x00FFFFFF
            animateColor({ backgroundColor }, { backgroundColor = it }, targetColor, duration, type, onComplete)
        }
        is meowing.zen.canvas.core.components.Text -> {
            val targetColor = (textColor and 0x00FFFFFF) or (255 shl 24)
            textColor = textColor and 0x00FFFFFF
            animateColor({ textColor }, { textColor = it }, targetColor, duration, type, onComplete)
        }
        else -> {
            animateFloat({ 0f }, {}, 1f, duration, type, AnimationType.ALPHA, onComplete)
        }
    }
}

fun <T : CanvasElement<T>> T.fadeOut(
    duration: Long = 300,
    type: EasingType = EasingType.EASE_IN,
    onComplete: (() -> Unit)? = null
): Animation<*> {
    return when (this) {
        is meowing.zen.canvas.core.components.Rectangle -> {
            val targetColor = backgroundColor and 0x00FFFFFF
            animateColor({ backgroundColor }, { backgroundColor = it }, targetColor, duration, type) {
                visible = false
                onComplete?.invoke()
            }
        }
        is meowing.zen.canvas.core.components.Text -> {
            val targetColor = textColor and 0x00FFFFFF
            animateColor({ textColor }, { textColor = it }, targetColor, duration, type) {
                visible = false
                onComplete?.invoke()
            }
        }
        else -> {
            animateFloat({ 1f }, {}, 0f, duration, type, AnimationType.ALPHA) {
                visible = false
                onComplete?.invoke()
            }
        }
    }
}

fun <T : CanvasElement<T>> T.slideIn(
    fromX: Float = -width,
    fromY: Float = 0f,
    duration: Long = 500,
    type: EasingType = EasingType.EASE_OUT,
    onComplete: (() -> Unit)? = null
): VectorAnimation {
    val elementId = this.hashCode().toString()
    elementOriginalPositions.putIfAbsent(elementId, xConstraint to yConstraint)

    val (targetX, targetY) = elementOriginalPositions[elementId]!!
    xConstraint = fromX
    yConstraint = fromY
    visible = true

    return animatePosition(targetX, targetY, duration, type, onComplete)
}

fun <T : CanvasElement<T>> T.bounceScale(
    scale: Float = 1.2f,
    duration: Long = 200,
    onComplete: (() -> Unit)? = null
): VectorAnimation {
    val elementId = this.hashCode().toString()
    Manager.stopAnimations(elementId, AnimationType.SIZE)
    elementOriginalSizes.putIfAbsent(elementId, width to height)

    val (originalWidth, originalHeight) = elementOriginalSizes[elementId]!!
    val targetWidth = originalWidth * scale
    val targetHeight = originalHeight * scale

    return animateSize(targetWidth, targetHeight, duration / 2, EasingType.EASE_OUT) {
        animateSize(originalWidth, originalHeight, duration / 2, EasingType.EASE_IN, onComplete)
    }
}