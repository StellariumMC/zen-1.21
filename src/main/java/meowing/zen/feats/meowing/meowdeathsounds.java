package meowing.zen.feats.meowing;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.featManager;
import meowing.zen.utils.utils;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;

public class meowdeathsounds {
    private static final meowdeathsounds instance = new meowdeathsounds();
    private meowdeathsounds() {}
    public static void initialize() {
        featManager.register(instance, () -> {
            EventBus.register(EventTypes.EntityUnloadEvent.class, instance, instance::onEntityUnload);
        });
    }

    private void onEntityUnload(EventTypes.EntityUnloadEvent event) {
        Entity ent = event.getEntity();
        if (ent.isAlive() || ent instanceof ArmorStandEntity || ent.isInvisible()) return;
        if (ent instanceof LivingEntity livingEntity && livingEntity.isDead()) {
            utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f);
            Vec3d pos = ent.getPos().add(0, 1, 0);
            for (int i = 0; i < 5; i++) {
                utils.spawnParticle(ParticleTypes.NOTE, pos.add(new Vec3d((Math.random() - 0.5) * 2, (Math.random() - 0.75), (Math.random() - 0.5) * 2)));
            }
        }
    }
}