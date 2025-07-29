package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.EntityHitResult
import java.awt.Color

@Zen.Module
object EntityHighlight : Feature("entityhighlight") {
    private val entityhighlightplayercolor by ConfigDelegate<Color>("entityhighlightplayercolor")
    private val entityhighlightmobcolor by ConfigDelegate<Color>("entityhighlightmobcolor")
    private val entityhighlightanimalcolor by ConfigDelegate<Color>("entityhighlightanimalcolor")
    private val entityhighlightothercolor by ConfigDelegate<Color>("entityhighlightothercolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlight",
                "Entity highlight",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightplayercolor",
                "Player color",
                ElementType.ColorPicker(Color(0, 255, 255, 255))
            ))
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightmobcolor",
                "Mob color",
                ElementType.ColorPicker(Color(255, 0, 0, 255))
            ))
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightanimalcolor",
                "Animal color",
                ElementType.ColorPicker(Color(0, 255, 0, 255))
            ))
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightothercolor",
                "Other entity color",
                ElementType.ColorPicker(Color(255, 255, 255, 255))
            ))
            .addElement("General", "Entity highlight", "Width", ConfigElement(
                "entityhighlightwidth",
                "Entity highlight width",
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
        val crosshairTarget = mc.crosshairTarget as? EntityHitResult ?: return false
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