package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import xyz.meowing.zen.features.visuals.ItemAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//#endif

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {
    //#if MC >= 1.21.9
    //$$ @Inject(
    //$$         method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
    //$$         )
    //$$ )
    //$$ public void zen$onRenderHeldItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
    //#else
    @Inject(
            method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    public void zen$onRenderHeldItem(
            AbstractClientPlayer player,
            float tickDelta,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        //#endif
        if (ItemAnimations.INSTANCE.isEnabled() && hand == InteractionHand.MAIN_HAND) {
            ItemAnimations.getItemTransform().apply(matrices);
        }
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
            )
    )
    public float zen$attackCooldown(float original) {
        return (ItemAnimations.INSTANCE.isEnabled()) ? 1f : original;
    }

    @Inject(
            method = "applyItemArmTransform",
            at = @At("HEAD"),
            cancellable = true
    )
    public void zen$onApplyEquipOffset(PoseStack matrices, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
        if (ItemAnimations.INSTANCE.isEnabled() && ItemAnimations.INSTANCE.getCancelReEquip()) {
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;
            matrices.translate((float)i * 0.56f, -0.52f, -0.72f);
            ci.cancel();
        }
    }

    @Inject(
            method = "applyEatTransform",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;pow(DD)D",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void zen$onDrink(PoseStack matrices, float tickDelta, HumanoidArm arm, ItemStack stack, Player player, CallbackInfo ci) {
        if (ItemAnimations.INSTANCE.isEnabled()) ci.cancel();
    }
}