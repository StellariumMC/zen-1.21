package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.RenderEvent;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Shadow
    private ClientWorld world;

    @Final
    @Shadow
    private BufferBuilderStorage bufferBuilders;

    @Unique
    @Nullable
    private MatrixStack capturedMatrixStack;

    @Inject(
            method = "renderTargetBlockOutline",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderTargetBlockOutline(
            VertexConsumerProvider.Immediate immediate,
            MatrixStack matrices,
            boolean renderBlockOutline,
            WorldRenderState renderStates,
            CallbackInfo ci
    ) {
        OutlineRenderState outlineRenderState = renderStates.outlineRenderState;
        if (outlineRenderState != null) {
            BlockPos blockPos = outlineRenderState.pos();
            BlockState blockState = this.world.getBlockState(blockPos);

            if (EventBus.INSTANCE.post(new RenderEvent.BlockOutline(blockPos, blockState, immediate, matrices))) ci.cancel();
        }
    }

    @Inject(
            method = "pushEntityRenders",
            at = @At("HEAD")
    )
    private void onWorldPostEntities(
            MatrixStack matrices,
            WorldRenderState renderStates,
            OrderedRenderCommandQueue queue,
            CallbackInfo ci
    ) {
        this.capturedMatrixStack = matrices;
        EventBus.INSTANCE.post(new RenderEvent.WorldPostEntities(this.bufferBuilders.getEntityVertexConsumers(), matrices));
    }

    @Inject(
            method = "renderMain",
            at = @At("RETURN")
    )
    private void onWorld(CallbackInfo ci) {
        EventBus.INSTANCE.post(new RenderEvent.World(this.bufferBuilders.getEntityVertexConsumers(), this.capturedMatrixStack));
    }

    @ModifyExpressionValue(
        method = "fillEntityRenderStates",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;hasOutline()Z")
    )
    private boolean zen$onEntityGlow(boolean original, @Local Entity entity) {
        RenderEvent.EntityGlow event = new RenderEvent.EntityGlow(entity, original, -1);
        EventBus.INSTANCE.post(event);
        return event.getShouldGlow();
    }
}