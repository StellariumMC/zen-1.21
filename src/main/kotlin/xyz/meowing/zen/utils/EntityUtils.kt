package xyz.meowing.zen.utils

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import kotlin.time.Duration

/**
 * Modified from SkyOcean's implementation
 *
 * Original File: [GitHub](https://github.com/meowdding/SkyOcean/blob/main/src/common/main/kotlin/me/owdding/skyocean/helpers/EntityHelper.kt)
 * @author Meowdding
 */
internal interface EntityAccessor {
    fun `zen$setGlowing`(glowing: Boolean)
    fun `zen$setGlowingColor`(color: Int)
    fun `zen$glowTime`(time: Long)
    fun `zen$setGlowingThisFrame`(glowing: Boolean)
}

var Entity.isGlowing: Boolean
    get() = this.isCurrentlyGlowing
    set(value) {
        (this as EntityAccessor).`zen$setGlowing`(value)
    }

var Entity.glowTime: Duration
    get() = Duration.INFINITE
    set(value) {
        (this as EntityAccessor).`zen$glowTime`(value.inWholeMilliseconds)
    }

var Entity.glowingColor: Int
    get() = this.teamColor
    set(value) {
        (this as EntityAccessor).`zen$setGlowingColor`(value)
    }

var Entity.glowThisFrame: Boolean
    get() = false
    set(value) {
        (this as EntityAccessor).`zen$setGlowingThisFrame`(value)
    }

val LivingEntity.baseMaxHealth: Int get() = this.getAttributeBaseValue(Attributes.MAX_HEALTH).toInt()