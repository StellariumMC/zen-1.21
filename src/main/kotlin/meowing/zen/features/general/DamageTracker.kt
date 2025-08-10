package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.WorldEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.text.Text
import java.util.Optional

@Zen.Module
object DamageTracker : Feature("damagetracker") {
    private val entities = mutableListOf<Int>()
    private val regex =  "[✧✯]?(\\d{1,3}(?:,\\d{3})*[⚔+✧❤♞☄✷ﬗ✯]*)".toRegex()
    private val selectedTypes by ConfigDelegate<Set<Int>>("damagetrackertype")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Damage tracker", ConfigElement(
                "damagetracker",
                "Damage tracker",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Damage tracker", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Logs damage dealt messages to chat when you hit a mob.")
            ))
            .addElement("General", "Damage tracker", "Options", ConfigElement(
                "damagetrackertype",
                "Hit detection types",
                ElementType.MultiCheckbox(
                    options = listOf("Crit Hits", "Overload Hits", "Fire Hits", "Non-Crit Hits"),
                    default = setOf(0)
                )
            ))
    }

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            if (entities.contains(event.packet.id)) return@register
            event.packet.trackedValues?.find { it.id == 2 && it.value is Optional<*> }?.let { obj ->
                val optional = obj.value as Optional<*>
                val name = (optional.orElse(null) as? Text)?.string
                val clean = name?.removeFormatting() ?: return@let
                if (regex.matches(clean) && shouldLog(name, clean)) ChatUtils.addMessage("$prefix $name")
            }
        }

        register<WorldEvent.Change> {
            entities.clear()
        }
    }

    private fun shouldLog(originalName: String, cleanName: String): Boolean {
        val isCrit = cleanName.contains("✧")
        val isOverload = cleanName.contains("✯")
        val isFire = originalName.contains("§6")

        return selectedTypes.any { selectedType ->
            when (selectedType) {
                0 -> isCrit
                1 -> isOverload
                2 -> isFire
                3 -> !isCrit && !isFire && !isOverload
                else -> false
            }
        }
    }
}