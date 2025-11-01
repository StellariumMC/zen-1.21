package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.meowing.zen.events.core.RenderEvent;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @ModifyExpressionValue(method = {"getEntitiesToRender", "renderEntities"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"), require = 2)
    private boolean zen$onEntityGlow(boolean original, @Local Entity entity) {
        RenderEvent.EntityGlow event = new RenderEvent.EntityGlow(entity, original, -1);
        EventBus.INSTANCE.post(event);
        return event.getShouldGlow();
    }

    @ModifyExpressionValue(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getTeamColorValue()I"))
    private int zen$onGlowColor(int color, @Local Entity entity) {
        RenderEvent.EntityGlow event = new RenderEvent.EntityGlow(entity, true, color);
        EventBus.INSTANCE.post(event);
        return event.getGlowColor() != -1 ? event.getGlowColor() : color;
    }
}