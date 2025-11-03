package xyz.meowing.zen.utils

import net.minecraft.entity.Entity
import kotlin.time.Duration

internal interface EntityAccessor {
    fun `zen$setGlowing`(glowing: Boolean)
    fun `zen$setGlowingColor`(color: Int)
    fun `zen$glowTime`(time: Long)
    fun `zen$setGlowingThisFrame`(glowing: Boolean)
}

var Entity.isCurrentlyGlowing: Boolean
    get() = this.isGlowing
    set(value) {
        (this as EntityAccessor).`zen$setGlowing`(value)
    }

var Entity.glowTime: Duration
    get() = Duration.INFINITE
    set(value) {
        (this as EntityAccessor).`zen$glowTime`(value.inWholeMilliseconds)
    }

var Entity.glowingColor: Int
    get() = this.teamColorValue
    set(value) {
        (this as EntityAccessor).`zen$setGlowingColor`(value)
    }

var Entity.glowThisFrame: Boolean
    get() = false
    set(value) {
        (this as EntityAccessor).`zen$setGlowingThisFrame`(value)
    }