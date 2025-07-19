package meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(value = YggdrasilMinecraftSessionService.class)
public class MixinYggdrasil {
    @WrapOperation(method = "getPropertySignatureState", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/properties/Property;hasSignature()Z"), remap = false)
    private boolean zen$hasSignature(Property instance, Operation<Boolean> original) {
        boolean hasSig = original.call(instance);
        try {
            if (hasSig && instance.signature() != null && instance.signature().isEmpty()) return false;
        } catch (Throwable e) {
            System.err.println("[Zen] Error in MixinYggdrasil: " + e);
        }
        return hasSig;
    }
}