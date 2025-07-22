package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.util.Optional

@Zen.Module
object MinibossSpawn : Feature("minibossspawn", true) {
    private val entities = mutableListOf<Int>()
    private val names = listOf(
        "Atoned Revenant ", "Atoned Champion ", "Deformed Revenant ", "Revenant Champion ", "Revenant Sycophant ",
        "Mutant Tarantula ", "Tarantula Beast ", "Tarantula Vermin ",
        "Sven Alpha ", "Sven Follower ", "Pack Enforcer ",
        "Voidcrazed Maniac ", "Voidling Radical ", "Voidling Devotee "
    )
    private val regex = "\\d[\\d.,]*[kKmMbBtT]?❤?$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Miniboss", ConfigElement(
                "minibossspawn",
                "Miniboss spawn alert",
                "Plays a sound when a miniboss spawns near you.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            if (entities.contains(event.packet.id)) return@register
            event.packet.trackedValues?.find { it.id == 2 && it.value is Optional<*> }?.let { obj ->
                val optional = obj.value as Optional<*>
                val name = (optional.orElse(null) as? Text)?.string ?: return@let
                val clean = name.removeFormatting().replace(regex, "")
                if (names.contains(clean)) {
                    Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 1f, 1f)
                    ChatUtils.addMessage("$prefix §b$clean§fspawned.")
                    entities.add(event.packet.id)
                }
            }
        }

        register<WorldEvent.Change> ({
            entities.clear()
        })
    }
}