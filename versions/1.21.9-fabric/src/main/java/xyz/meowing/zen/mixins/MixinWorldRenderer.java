package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.core.RenderEvent;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
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