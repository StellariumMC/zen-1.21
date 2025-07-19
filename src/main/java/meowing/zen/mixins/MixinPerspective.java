package meowing.zen.mixins;

import meowing.zen.feats.general.removeselfiecam;
import net.minecraft.client.option.Perspective;
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
@Mixin(Perspective.class)
public class MixinPerspective {
    @Shadow @Final private boolean firstPerson;

    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    private void zen$onPrespectiveChange(CallbackInfoReturnable<Perspective> cir) {
        if (removeselfiecam.INSTANCE.isEnabled()) cir.setReturnValue(this.firstPerson ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON);
    }
}