package meowing.zen.feats.meowing;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.featManager;
import meowing.zen.utils.meowutils;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;

public class meowdeathsounds {
    public static void initialize() {
        featManager.register(new meowdeathsounds(), () -> {
            EventBus.register(EventTypes.EntityUnloadEvent.class, meowdeathsounds.class, meowdeathsounds::handleEntityUnload);
        });
    }

    private static void handleEntityUnload(EventTypes.EntityUnloadEvent event) {
        Entity ent = event.entity;
        if (ent.isAlive() || ent instanceof ArmorStandEntity || 
            ent.isInvisible() || !(ent instanceof LivingEntity livingEntity && livingEntity.isDead())) return;
        
        meowutils.playMeow();
    }
}