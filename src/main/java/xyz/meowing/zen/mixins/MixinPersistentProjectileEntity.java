package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.EntityEvent;

@Mixin(PersistentProjectileEntity.class)
public class MixinPersistentProjectileEntity {

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onArrowHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
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