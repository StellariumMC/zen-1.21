package meowing.zen.feats.meowing

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import meowing.zen.featManager
import meowing.zen.utils.utils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.minecraft.entity.LivingEntity
import net.minecraft.particle.ParticleTypes
import kotlin.random.Random

class meowdeathsounds private constructor() {
    companion object {
        private val instance = meowdeathsounds()

        @JvmStatic
        fun initialize() {
            featManager.register(instance) {
                EventBus.register(EventTypes.EntityUnloadEvent::class.java, instance, instance::onEntityUnload)
            }
        }
    }

    private fun onEntityUnload(event: EventTypes.EntityUnloadEvent) {
        val ent = event.entity
        if (ent.isAlive || ent is ArmorStandEntity || ent.isInvisible) return

        if (ent is LivingEntity && ent.isDead) {
            utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f)
            val pos = ent.pos.add(0.0, 1.0, 0.0)
            repeat(5) {
                utils.spawnParticle(
                    ParticleTypes.NOTE,
                    pos.add(Vec3d(
                        (Random.nextDouble() - 0.5) * 2,
                        Random.nextDouble() - 0.75,
                        (Random.nextDouble() - 0.5) * 2
                    ))
                )
            }
        }
    }
}