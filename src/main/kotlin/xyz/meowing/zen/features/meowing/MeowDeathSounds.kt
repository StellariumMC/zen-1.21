package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.utils.Utils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.sound.SoundEvents
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object MeowDeathSounds : Feature("meowdeathsounds") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Meow Death Sounds", "Meow Death Sounds", "Meowing", ConfigElement(
                "meowdeathsounds",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Plays a meow sound when a mob dies.", "", ConfigElement(
                "",
                ElementType.TextParagraph("Plays a meow sound when a mob dies.")
            ))
    }


    override fun initialize() {
        register<EntityEvent.Death> { event ->
            val entity = event.entity
            if (entity is ArmorStandEntity || entity.isInvisible) return@register
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f)
        }
    }
}