package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.Utils.toColorFloat
import net.minecraft.world.entity.decoration.ArmorStand
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

@Module
object KeyHighlight : Feature(
    "keyHighlight",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private val keyHighlightColor by ConfigDelegate<Color>("keyHighlight.color")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Key highlight",
                "Highlights Keys in dungeon",
                "Dungeons",
                ConfigElement(
                    "keyHighlight",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Key highlight color",
                ConfigElement(
                    "keyHighlight.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }

    override fun initialize() {
        register<RenderEvent.Entity.Pre> { event ->
            if (event.entity !is ArmorStand) return@register
            val name = event.entity.name.string.removeFormatting()

            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                val color = keyHighlightColor
                Render3D.drawEntityFilled(
                    event.matrices,
                    event.vertex,
                    entity.x,
                    entity.y + 1.25,
                    entity.z,
                    1f,
                    1f,
                    color.red.toColorFloat(),
                    color.green.toColorFloat(),
                    color.blue.toColorFloat(),
                    color.alpha.toColorFloat()
                )
            }
        }
    }
}