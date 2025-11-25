package xyz.meowing.zen.features.general

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
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
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object EntityHighlight : Feature(
    "entityHighlight"
) {
    private val playerColor by ConfigDelegate<Color>("entityHighlight.playerColor")
    private val mobColor by ConfigDelegate<Color>("entityHighlight.mobColor")
    private val animalColor by ConfigDelegate<Color>("entityHighlight.animalColor")
    private val otherColor by ConfigDelegate<Color>("entityHighlight.otherColor")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Entity highlight",
                "Highlights entities that you're looking at",
                "General",
                ConfigElement(
                    "entityHighlight",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Player color",
                ConfigElement(
                    "entityHighlight.playerColor",
                    ElementType.ColorPicker(Color(0, 255, 255, 255))
                )
            )
            .addFeatureOption(
                "Mob color",
                ConfigElement(
                    "entityHighlight.mobColor",
                    ElementType.ColorPicker(Color(255, 0, 0, 255))
                )
            )
            .addFeatureOption(
                "Animal color",
                ConfigElement(
                    "entityHighlight.animalColor",
                    ElementType.ColorPicker(Color(0, 255, 0, 255))
                )
            )
            .addFeatureOption(
                "Other entity color",
                ConfigElement(
                    "entityHighlight.otherColor",
                    ElementType.ColorPicker(Color(255, 255, 255, 255))
                )
            )
    }

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