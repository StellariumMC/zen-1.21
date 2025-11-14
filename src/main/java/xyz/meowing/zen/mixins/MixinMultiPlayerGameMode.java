package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.EntityEvent;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
    @Inject(
            method = "attack",
            at = @At("HEAD")
    )
    private void zen$onAttackEntity(
            Player player,
            Entity target,
            CallbackInfo callbackInfo
    ) {
        EventBus.INSTANCE.post(new EntityEvent.Attack(player, target));
    }
}
