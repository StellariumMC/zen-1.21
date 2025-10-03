package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.Utils.toColorFloat
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

@Zen.Module
object KeyHighlight : Feature("keyhighlight", area = "catacombs") {
    private val keyhighlightcolor by ConfigDelegate<Color>("keyhighlightcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Key Highlight", ConfigElement(
                "keyhighlight",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Key Highlight", "Color", ConfigElement(
                "keyhighlightcolor",
                "Key highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<RenderEvent.Entity.Pre> { event ->
            if (event.entity !is ArmorStandEntity) return@register
            val name = event.entity.name.string.removeFormatting()
            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                val color = keyhighlightcolor
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