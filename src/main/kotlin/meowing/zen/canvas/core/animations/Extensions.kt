package meowing.zen.canvas.core.animations

import meowing.zen.canvas.core.CanvasElement

private val elementOriginalPositions = mutableMapOf<String, Pair<Float, Float>>()
private val elementOriginalSizes = mutableMapOf<String, Pair<Float, Float>>()

fun <T : CanvasElement<T>> T.animateFloat(
    getter: () -> Float,
    setter: (Float) -> Unit,
    endValue: Float,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    animationType: AnimationType = AnimationType.CUSTOM,
    onComplete: (() -> Unit)? = null
): FloatAnimation {
    val target = AnimationTarget(getter(), endValue) { value -> setter(value) }
    val animation = FloatAnimation(target, duration, type, animationType, "${this.hashCode()}+${endValue}", onComplete)
    animation.start()
    return animation
}

fun <T : CanvasElement<T>> T.animateColor(
    getter: () -> Int,
    setter: (Int) -> Unit,
    endValue: Int,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): ColorAnimation {
    val target = AnimationTarget(getter(), endValue) { value -> setter(value) }
    val animation = ColorAnimation(target, duration, type, "${this.hashCode()}+${endValue}", onComplete)
    animation.start()
    return animation
}

fun <T : CanvasElement<T>> T.animatePosition(
    endX: Float,
    endY: Float,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): VectorAnimation {
    val elementId = "${this.hashCode()}:pos:${endX}:${endY}"
    elementOriginalPositions.putIfAbsent(elementId, x to y)

    val target = AnimationTarget(
        xConstraint to yConstraint,
        endX to endY
    ) { (newX, newY) ->
        xConstraint = newX
        yConstraint = newY
    }

    val animation = VectorAnimation(target, duration, type, AnimationType.POSITION, elementId, onComplete)
    animation.start()
    return animation
}

fun <T : CanvasElement<T>> T.animateSize(
    endWidth: Float,
    endHeight: Float,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): VectorAnimation {
    val elementId = "${this.hashCode()}+${endWidth+endHeight}"
    elementOriginalSizes.putIfAbsent(elementId, width to height)

    val target = AnimationTarget(
        width to height,
        endWidth to endHeight
    ) { (newWidth, newHeight) ->
        width = newWidth
        height = newHeight
    }

    val animation = VectorAnimation(target, duration, type, AnimationType.SIZE, elementId, onComplete)
    animation.start()
    return animation
}