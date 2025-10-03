package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.RenderEvent;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC >= 1.21.9
//$$ import java.util.HashMap;
//$$ import java.util.Map;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import net.minecraft.client.render.entity.state.EntityRenderState;
//#endif

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    //#if MC >= 1.21.9
    //$$ @Unique
    //$$ private final Map<EntityRenderState, Entity> zen$entityMap = new HashMap<>();
    //$$
    //$$ @Inject(method = "fillEntityRenderStates", at = @At("HEAD"))
    //$$ private void zen$clearEntityMap(CallbackInfo ci) {
    //$$     zen$entityMap.clear();
    //$$ }
    //$$
    //$$ @Inject(
    //$$     method = "fillEntityRenderStates",
    //$$     at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE)
    //$$ )
    //$$ private void zen$storeEntityMapping(CallbackInfo ci, @Local Entity entity, @Local EntityRenderState entityRenderState) {
    //$$     zen$entityMap.put(entityRenderState, entity);
    //$$ }
    //$$
    //$$ @ModifyExpressionValue(
    //$$     method = "fillEntityRenderStates",
    //$$     at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;hasOutline()Z")
    //$$ )
    //$$ private boolean zen$onEntityGlow(boolean original, @Local Entity entity) {
    //$$     RenderEvent.EntityGlow event = new RenderEvent.EntityGlow(entity, original, -1);
    //$$     EventBus.INSTANCE.post(event);
    //$$     return event.getShouldGlow();
    //$$ }
    //$$
    // TODO: Impl Glow Color
    //#else

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

    //#endif
}