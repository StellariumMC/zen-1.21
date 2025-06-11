package meowing.zen.feats.meowing;

import meowing.zen.Zen;
import meowing.zen.utils.meowutils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.LivingEntity;

public class meowdeathsounds {
    public static void initialize() {
        ClientEntityEvents.ENTITY_UNLOAD.register((ent, world) -> {
            if (!Zen.getConfig().meowdeathsounds || ent.isAlive() || ent instanceof ArmorStandEntity || ent.isInvisible() || !(ent instanceof LivingEntity livingEntity && livingEntity.isDead())) return;
            meowutils.playMeow();
        });
    }
}
