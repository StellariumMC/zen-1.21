package xyz.meowing.zen.mixins;

import xyz.meowing.zen.features.qol.RemoveSelfieCam;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(CameraType.class)
public class MixinCameraType {
    @Shadow
    @Final
    private boolean firstPerson;

    @Inject(
            method = "cycle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$onCycle(
            CallbackInfoReturnable<CameraType> cir
    ) {
        if (RemoveSelfieCam.INSTANCE.isEnabled()) {
            cir.setReturnValue(this.firstPerson ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
        }
    }
}