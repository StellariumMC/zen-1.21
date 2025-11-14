package xyz.meowing.zen.mixins;

//#if MC < 1.21.9
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.RenderEvent;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$onEntityRenderPre(
            Entity entity,
            double x,
            double y,
            double z,
            float tickProgress,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo callbackInfo
    ) {
        if (entity == null) return;
        RenderEvent.Entity.Pre event = new RenderEvent.Entity.Pre(entity, matrices, vertexConsumers, light);
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) callbackInfo.cancel();
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("TAIL")
    )
    private void zen$onEntityRenderPost(
            Entity entity,
            double x,
            double y,
            double z,
            float tickProgress,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo callbackInfo
    ) {
        if (entity == null) return;
        RenderEvent.Entity.Post event = new RenderEvent.Entity.Post(entity, matrices, vertexConsumers, light);
        EventBus.INSTANCE.post(event);
    }
}
//#endif