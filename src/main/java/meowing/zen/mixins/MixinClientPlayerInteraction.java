package meowing.zen.mixins;

import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteraction {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void zen$onAttackEntity(PlayerEntity player, Entity target, CallbackInfo callbackInfo) {
        EventBus.INSTANCE.post(new EntityEvent.Attack(player, target));
    }
}
