package meowing.zen.features.qol

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.features.Feature
import net.minecraft.entity.Entity

@Zen.Module
object HideDeathAnimation : Feature("hidedeathanimation") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "Hide death animation", ConfigElement(
                "hidedeathanimation",
                "Hide death animation",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<EntityEvent.Death> { event ->
            if (world != null && !event.entity.name.string.contains(" Livid")) {
                world!!.removeEntity(event.entity.id, Entity.RemovalReason.DISCARDED)
            }
        }
    }
}