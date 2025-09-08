package meowing.zen.mixins;

import meowing.zen.events.EntityEvent;
import meowing.zen.events.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Shadow
    @Final
    protected MinecraftClient client;

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(boolean dropAll, CallbackInfoReturnable<ItemEntity> cir) {
        if (client.player == null) return;

        ItemStack stack = client.player.getInventory().getSelectedStack();
        if (stack != null && EventBus.INSTANCE.post(new EntityEvent.ItemToss(stack))) cir.setReturnValue(null);
    }
}