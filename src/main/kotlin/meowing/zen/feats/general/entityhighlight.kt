package meowing.zen.feats.general

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.EntityHitResult
import java.awt.Color

object entityhighlight : Feature("entityhighlight") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlight",
                "Entity highlight",
                "Highlights the entity you are looking at",
                ElementType.Switch(false)
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightplayercolor",
                "Player color",
                "Color for highlighted players",
                ElementType.ColorPicker(Color(0, 255, 255, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightmobcolor",
                "Mob color",
                "Color for highlighted mobs",
                ElementType.ColorPicker(Color(255, 0, 0, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightanimalcolor",
                "Animal color",
                "Color for highlighted animals",
                ElementType.ColorPicker(Color(0, 255, 0, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightothercolor",
                "Other entity color",
                "Color for other highlighted entities",
                ElementType.ColorPicker(Color(255, 255, 255, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            val entity = event.entity
            if (entity == mc.player || entity.isInvisible) return@register

            val entityHitResult = mc.crosshairTarget as? EntityHitResult ?: return@register
            if (entityHitResult.entity != entity) return@register

            val color = when (entity) {
                is PlayerEntity -> config.entityhighlightplayercolor
                is PassiveEntity -> config.entityhighlightanimalcolor
                is MobEntity -> config.entityhighlightmobcolor
                else -> config.entityhighlightothercolor
            }
            event.shouldGlow = true
            event.glowColor = color.toColorInt()
        }
    }
}