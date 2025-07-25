package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.removeFormatting
import net.minecraft.text.Text
import java.util.Optional

@Zen.Module
object DamageTracker : Feature("damagetracker") {
    private val entities = mutableListOf<Int>()
    private val regex = Regex("\\s|^§\\w\\D$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Damage tracker", ConfigElement(
                "damagetracker",
                "Damage tracker",
                "Sends the damage done by you and other players in a certain area in chat.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            if (entities.contains(event.packet.id)) return@register
            event.packet.trackedValues?.find { it.id == 2 && it.value is Optional<*> }?.let { obj ->
                val optional = obj.value as Optional<*>
                val name = (optional.orElse(null) as? Text)?.string?.removeFormatting() ?: return@let
                if (name.isNotBlank() && !name.matches(regex)) ChatUtils.addMessage("$prefix $name")
            }
        }

        register<WorldEvent.Change> {
            entities.clear()
        }
    }
}