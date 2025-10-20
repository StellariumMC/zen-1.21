package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.EntityHitResult
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import java.awt.Color

@Zen.Module
object EntityHighlight : Feature("entityhighlight") {
    private val entityhighlightplayercolor by ConfigDelegate<Color>("entityhighlightplayercolor")
    private val entityhighlightmobcolor by ConfigDelegate<Color>("entityhighlightmobcolor")
    private val entityhighlightanimalcolor by ConfigDelegate<Color>("entityhighlightanimalcolor")
    private val entityhighlightothercolor by ConfigDelegate<Color>("entityhighlightothercolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Entity highlight", "Entity highlight", "General", ConfigElement(
                "entityhighlight",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Player color", "Player color", "Color", ConfigElement(
                "entityhighlightplayercolor",
                ElementType.ColorPicker(Color(0, 255, 255, 255))
            ))
            .addFeatureOption("Mob color", "Mob color", "Color", ConfigElement(
                "entityhighlightmobcolor",
                ElementType.ColorPicker(Color(255, 0, 0, 255))
            ))
            .addFeatureOption("Animal color", "Animal color", "Color", ConfigElement(
                "entityhighlightanimalcolor",
                ElementType.ColorPicker(Color(0, 255, 0, 255))
            ))
            .addFeatureOption("Other entity color", "Other entity color", "Color", ConfigElement(
                "entityhighlightothercolor",
                ElementType.ColorPicker(Color(255, 255, 255, 255))
            ))
            .addFeatureOption("Entity highlight width", "Entity highlight width", "Width", ConfigElement(
                    "entityhighlightwidth",
                    ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            val entity = event.entity
            val player = player ?: return@register

            if (entity == player || entity.isInvisible || !isEntityUnderCrosshair(entity)) return@register

            val color = getEntityColor(entity)
            event.shouldGlow = true
            event.glowColor = color.toColorInt()
        }
    }

    private fun isEntityUnderCrosshair(entity: Entity): Boolean {
        val crosshairTarget = client.crosshairTarget as? EntityHitResult ?: return false
        return crosshairTarget.entity == entity
    }

    private fun getEntityColor(entity: Entity): Color {
        return when (entity) {
            is PlayerEntity -> entityhighlightplayercolor
            is PassiveEntity -> entityhighlightanimalcolor
            is MobEntity -> entityhighlightmobcolor
            else -> entityhighlightothercolor
        }
    }
}