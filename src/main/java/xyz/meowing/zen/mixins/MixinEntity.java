package xyz.meowing.zen.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.meowing.zen.utils.EntityAccessor;

@Mixin(Entity.class)
public class MixinEntity implements EntityAccessor {
    @Unique
    private boolean zen$glowing = false;
    @Unique
    private int zen$glowingColor = 0;
    @Unique
    private long zen$glowTime = -1;
    @Unique
    private boolean zen$glowingThisFrame = false;

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    public void getTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (hasCustomGlow()) {
            cir.setReturnValue(zen$glowingColor);
            this.zen$glowingThisFrame = false;
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (hasCustomGlow()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void zen$setGlowing(boolean glowing) {
        this.zen$glowing = glowing;
    }

    @Override
    public void zen$setGlowingColor(int color) {
        this.zen$glowingColor = color;
    }

    @Override
    public void zen$glowTime(long time) {
        this.zen$glowTime = System.currentTimeMillis() + time;
        this.zen$glowing = false;
    }

    @Override
    public void zen$setGlowingThisFrame(boolean glowing) {
        this.zen$glowingThisFrame = glowing;
    }

    @Unique
    private boolean hasCustomGlow() {
        if (this.zen$glowingThisFrame) return true;
        if (this.zen$glowTime > System.currentTimeMillis()) return true;
        this.zen$glowTime = -1;
        return this.zen$glowing;
    }
}