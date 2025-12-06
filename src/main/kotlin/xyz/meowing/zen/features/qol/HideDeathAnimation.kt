package xyz.meowing.zen.features.qol

import xyz.meowing.zen.features.Feature
import net.minecraft.world.entity.Entity
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent

@Module
object HideDeathAnimation : Feature(
    "hideDeathAnimation",
    "Hide death animation",
    "Hides the death animation for the mobs",
    "QoL"
) {
    override fun initialize() {
        register<EntityEvent.Death> { event ->
            if (!event.entity.name.string.contains(" Livid")) {
                world?.removeEntity(event.entity.id, Entity.RemovalReason.DISCARDED)
            }
        }
    }
}