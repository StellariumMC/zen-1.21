package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.features.Feature
import net.minecraft.entity.Entity

@Zen.Module
object HideDeathAnimation : Feature("hidedeathanimation") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Hide death animation", "Hide death animation", "QoL", xyz.meowing.zen.ui.ConfigElement(
                "hidedeathanimation",
                ElementType.Switch(false)
            ))
        return configUI
    }


    override fun initialize() {
        register<EntityEvent.Death> { event ->
            if (world != null && !event.entity.name.string.contains(" Livid")) {
                world!!.removeEntity(event.entity.id, Entity.RemovalReason.DISCARDED)
            }
        }
    }
}