package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.meowing.zen.events.core.EntityEvent;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Inject(
            method = "drop",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onDropItem(
            boolean dropAll,
            CallbackInfoReturnable<ItemEntity> cir
    ) {
        if (this.minecraft.player == null) return;

        ItemStack stack = this.minecraft.player.getInventory().getSelectedItem();
        if (EventBus.INSTANCE.post(new EntityEvent.ItemToss(stack))) cir.setReturnValue(null);
    }
}