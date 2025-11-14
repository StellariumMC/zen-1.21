package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.EntityEvent;

@Mixin(AbstractArrow.class)
public class MixinAbstractArrow {

    @Inject(
            method = "onHitEntity",
            at = @At("HEAD")
    )
    private void onArrowHitEntity(
            EntityHitResult entityHitResult,
            CallbackInfo ci
    ) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        Entity shooter = arrow.getOwner();
        Entity target = entityHitResult.getEntity();

        if (shooter != null && target != null) {
            String shooterName = shooter.getName().getString();
            if (shooterName != null) {
                EventBus.INSTANCE.post(new EntityEvent.ArrowHit(shooterName, target));
            }
        }
    }
}