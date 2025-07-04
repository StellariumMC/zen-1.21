package meowing.zen.feats.general

import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.text.Text
import java.util.Optional

object damagetracker : Feature("damagetracker") {
    private val regex = "\\s|^§\\w\\D$".toRegex()

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            event.packet.trackedValues?.find { it.id == 2 && it.value is Optional<*> }?.let { obj ->
                val optional = obj.value as Optional<*>
                val name = (optional.orElse(null) as? Text)?.string ?: return@let
                if (name.removeFormatting().matches(regex)) ChatUtils.addMessage("§c[Zen] $name")
            }
        }
    }
}