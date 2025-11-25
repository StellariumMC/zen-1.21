package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.utils.Utils
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.sounds.SoundEvents
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object MeowDeathSounds : Feature(
    "meowDeathSounds"
) {
    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Meow death sounds",
                "Plays a meow sound when a mob dies",
                "Meowing",
                ConfigElement(
                    "meowDeathSounds",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<EntityEvent.Death> { event ->
            val entity = event.entity
            if (entity is ArmorStand || entity.isInvisible) return@register
            Utils.playSound(SoundEvents.CAT_AMBIENT, 0.8f, 1.0f)
        }
    }
}