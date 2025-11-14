package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.core.RenderEvent;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.WeakHashMap;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Unique
    private static final WeakHashMap<EntityRenderState, Entity> krypt$stateToEntity = new WeakHashMap<>();

    @Inject(
            method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private void krypt$associateStateWithEntity(T entity, float partialTick, CallbackInfoReturnable<S> cir) {
        krypt$stateToEntity.put(cir.getReturnValue(), entity);
    }

    @Inject(
            method = "submit",
            at = @At("HEAD"),
            cancellable = true
    )
    private void krypt$onEntityRenderPre(
            S state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState,
            CallbackInfo ci
    ) {
        Entity entity = krypt$stateToEntity.get(state);
        if (entity == null) return;

        RenderEvent.Entity.Pre event = new RenderEvent.Entity.Pre(
                entity,
                poseStack,
                null,
                state.lightCoords
        );
        EventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "submit",
            at = @At("TAIL")
    )
    private void krypt$onEntityRenderPost(
            S state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState,
            CallbackInfo ci
    ) {
        Entity entity = krypt$stateToEntity.get(state);
        if (entity == null) return;

        RenderEvent.Entity.Post event = new RenderEvent.Entity.Post(
                entity,
                poseStack,
                null,
                state.lightCoords
        );
        EventBus.INSTANCE.post(event);

        krypt$stateToEntity.remove(state);
    }
}