package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.utils.Utils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvents
import kotlin.random.Random

@Zen.Module
object meowdeathsounds : Feature("meowdeathsounds") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow Sounds", ConfigElement(
                "meowsounds",
                "Meow Sounds",
                "Plays a cat sound whenever someone sends \"meow\" in chat",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Leave> {
            val entity = it.entity
            if (entity is ArmorStandEntity || entity.isInvisible || entity.isAlive) return@register

            val pos = entity.pos
            mc.world?.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_CAT_AMBIENT,
                net.minecraft.sound.SoundCategory.AMBIENT,
                0.8f, 1.0f
            )

            repeat(5) {
                Utils.spawnParticle(
                    ParticleTypes.NOTE,
                    pos.x + (Random.nextDouble() - 0.5),
                    pos.y + 1.0 + Random.nextDouble() * 0.5,
                    pos.z + (Random.nextDouble() - 0.5),
                    0.0, 0.2, 0.0
                )
            }
        }
    }
}