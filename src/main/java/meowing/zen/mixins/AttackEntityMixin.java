package meowing.zen.mixins;

import meowing.zen.events.AttackEntityEvent;
import meowing.zen.events.EventBus;
import meowing.zen.utils.ChatUtils;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class AttackEntityMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo callbackInfo) {
        EventBus.INSTANCE.post(new AttackEntityEvent(player, target));
    }
}
