package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import tech.thatgravyboat.skyblockapi.api.datatype.DataType;
import tech.thatgravyboat.skyblockapi.impl.DataTypesRegistry;
import xyz.meowing.knit.api.KnitPlayer;
import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.core.EntityEvent;
import xyz.meowing.zen.features.visuals.ItemAnimations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    @Shadow
    public float attackAnim;
    @Unique
    private int zen$animationTicks;

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setPose(Lnet/minecraft/world/entity/Pose;)V"
            )
    )
    private void zen$onDeath(
            DamageSource damageSource,
            CallbackInfo ci
    ) {
        EventBus.INSTANCE.post(new EntityEvent.Death(this));
    }

    @WrapMethod(
            method = "updateSwingTime"
    )
    private void zen$modifySwingPos(
            Operation<Void> original
    ) {
        if (!ItemAnimations.INSTANCE.isEnabled()) {
            original.call();
            return;
        }

        if (ItemAnimations.INSTANCE.getNoSwing()) {
            this.attackAnim = 0F;
            zen$animationTicks = 0;
            return;
        }

        if (ItemAnimations.noSwingTerm()) {
            this.attackAnim = 0F;
            zen$animationTicks = 0;
            return;
        }

        double speedMultiplier = ItemAnimations.INSTANCE.getSwingSpeed();
        if (speedMultiplier == 0.0) {
            original.call();
            return;
        }

        int swingDuration = (int) (6 / Math.max(0.1, speedMultiplier + 1.0));

        if (zen$animationTicks > swingDuration) zen$animationTicks = 0;

        this.attackAnim = zen$animationTicks == 0 ? 1F : (zen$animationTicks - 1F) / swingDuration;
        if (zen$animationTicks > 0) zen$animationTicks++;
    }

    @Inject(
            method = "swing(Lnet/minecraft/world/InteractionHand;Z)V",
            at = @At("HEAD")
    )
    public void zen$onSwing(
            InteractionHand hand,
            boolean fromServerPlayer,
            CallbackInfo ci
    ) {
        if (zen$animationTicks == 0 && ItemAnimations.INSTANCE.isEnabled() && !ItemAnimations.INSTANCE.getNoSwing()) {
            zen$animationTicks = 1;
        }
    }
}