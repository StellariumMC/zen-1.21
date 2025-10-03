package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.RenderEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.GuardianEntityRenderer;
import net.minecraft.client.render.entity.state.GuardianEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.21.9
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//#endif

@Mixin(GuardianEntityRenderer.class)
public class MixinGuardianEntityRenderer {
    @Unique private GuardianEntity zen$currentEntity;

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/mob/GuardianEntity;Lnet/minecraft/client/render/entity/state/GuardianEntityRenderState;F)V", at = @At("HEAD"))
    private void zen$captureEntity(GuardianEntity entity, GuardianEntityRenderState renderState, float tickDelta, CallbackInfo ci) {
        this.zen$currentEntity = entity;
    }

    //#if MC >= 1.21.9
    //$$ @Inject(
    //$$         method = "render(Lnet/minecraft/client/render/entity/state/GuardianEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
    //$$         at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", ordinal = 0),
    //$$          cancellable = true
    //$$ )
    //$$ private void zen$guardianLaserRender(GuardianEntityRenderState guardianEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
    //#else
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/GuardianEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", ordinal = 0), cancellable = true)
    private void zen$guardianLaserRender(GuardianEntityRenderState renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
    //#endif
        if (zen$currentEntity != null && EventBus.INSTANCE.post(new RenderEvent.GuardianLaser(zen$currentEntity, zen$currentEntity.getBeamTarget()))) {
            ci.cancel();
        }
    }
}