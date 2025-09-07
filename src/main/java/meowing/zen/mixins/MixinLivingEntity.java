package meowing.zen.mixins;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import meowing.zen.features.visuals.ItemAnimations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    @Shadow public float handSwingProgress;
    @Unique private int zen$animationTicks;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    /*
     * Modified from Devonian code
     * Under GPL 3.0 License
     */
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
    private void zen$onDeath(DamageSource damageSource, CallbackInfo ci) {
        EventBus.INSTANCE.post(new EntityEvent.Death(this));
    }

    @WrapMethod(method = "tickHandSwing")
    private void zen$modifySwingPos(Operation<Void> original) {
        double speedMultiplier = ItemAnimations.INSTANCE.getSwingSpeed();
        if (speedMultiplier == 0.0 || !ItemAnimations.INSTANCE.isEnabled()) {
            original.call();
            return;
        }

        int swingDuration = (int) (6 / Math.max(0.1, speedMultiplier + 1.0));

        if (zen$animationTicks > swingDuration) zen$animationTicks = 0;

        handSwingProgress = zen$animationTicks == 0 ? 1F : (zen$animationTicks - 1F) / swingDuration;
        if (zen$animationTicks > 0) zen$animationTicks++;
    }

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"))
    public void zen$onSwing(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
        if (zen$animationTicks == 0 && ItemAnimations.INSTANCE.isEnabled()) zen$animationTicks = 1;
    }
}