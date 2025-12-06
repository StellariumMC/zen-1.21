package xyz.meowing.zen.features.general

import xyz.meowing.zen.features.Feature
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object EntityHighlight : Feature(
    "entityHighlight",
    "Entity highlight",
    "Highlights entities that you're looking at",
    "General"
) {
    private val playerColor by config.colorPicker("Player color", Color(0, 255, 255, 255))
    private val mobColor by config.colorPicker("Mob color", Color(255, 0, 0, 255))
    private val animalColor by config.colorPicker("Animal color", Color(0, 255, 0, 255))
    private val otherColor by config.colorPicker("Other color", Color(255, 255, 255, 255))

    override fun initialize() {
        register<RenderEvent.Entity.Pre> { event ->
            val entity = event.entity
            val player = player ?: return@register
            if (entity == player || entity.isInvisible || !isEntityUnderCrosshair(entity)) return@register

            entity.glowThisFrame = true
            entity.glowingColor = getEntityColor(entity).rgb
        }
    }

    private fun isEntityUnderCrosshair(entity: Entity): Boolean {
        val crosshairTarget = client.hitResult as? EntityHitResult ?: return false
        return crosshairTarget.entity == entity
    }

    private fun getEntityColor(entity: Entity): Color {
        return when (entity) {
            is Player -> playerColor
            is AgeableMob -> animalColor
            is Mob -> mobColor
            else -> otherColor
        }
    }
}