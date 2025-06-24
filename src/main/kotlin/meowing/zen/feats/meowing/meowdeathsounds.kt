package meowing.zen.feats.meowing

import meowing.zen.events.EntityLeaveEvent
import meowing.zen.feats.Feature
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvents
import kotlin.random.Random

object meowdeathsounds : Feature("meowdeathsounds") {
    override fun initialize() {
        register<EntityLeaveEvent> {
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