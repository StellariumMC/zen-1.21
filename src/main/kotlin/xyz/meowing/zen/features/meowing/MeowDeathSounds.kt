package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.utils.Utils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.sound.SoundEvents

@Zen.Module
object MeowDeathSounds : Feature("meowdeathsounds") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Meow Death Sounds", "Meow Death Sounds", "Meowing", xyz.meowing.zen.ui.ConfigElement(
                "meowdeathsounds",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Plays a meow sound when a mob dies.", "", xyz.meowing.zen.ui.ConfigElement(
                "",
                ElementType.TextParagraph("Plays a meow sound when a mob dies.")
            ))
        return configUI
    }


    override fun initialize() {
        register<EntityEvent.Death> { event ->
            val entity = event.entity
            if (entity is ArmorStandEntity || entity.isInvisible) return@register
            Utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f)
        }
    }
}