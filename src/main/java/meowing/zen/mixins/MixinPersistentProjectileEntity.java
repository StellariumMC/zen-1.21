package meowing.zen.mixins;

import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import meowing.zen.utils.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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