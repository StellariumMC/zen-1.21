package xyz.meowing.zen.features.qol

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.entity.Entity
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object HideDeathAnimation : Feature(
    "hideDeathAnimation"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Hide death animation",
                "Hides the death animation for the mobs",
                "QoL",
                ConfigElement(
                    "hideDeathAnimation",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<EntityEvent.Death> { event ->
            if (!event.entity.name.string.contains(" Livid")) {
                world?.removeEntity(event.entity.id, Entity.RemovalReason.DISCARDED)
            }
        }
    }
}