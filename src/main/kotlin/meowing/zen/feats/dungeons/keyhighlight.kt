package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.Utils.toColorFloat
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

object keyhighlight : Feature("keyhighlight", area = "catacombs") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyalert",
                "Key spawn alert",
                "Displays a title when the wither/blood key spawns",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyhighlight",
                "Key highlight",
                "Highlights the wither/blood key",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyhighlightcolor",
                "Key highlight color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["keyhighlight"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        var color = Color(0, 255, 255, 127)

        Zen.registerCallback("keyhighlightcolor") { newval ->
            color = newval as Color
        }

        register<RenderEvent.EntityPre> { event ->
            if (event.entity !is ArmorStandEntity) return@register
            val name = event.entity.name.string.removeFormatting()
            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                RenderUtils.renderEntityFilled(
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