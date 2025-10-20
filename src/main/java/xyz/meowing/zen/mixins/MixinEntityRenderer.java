package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//#if MC >= 1.21.9
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import net.minecraft.entity.Entity;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.render.entity.state.EntityRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//$$ import xyz.meowing.zen.events.RenderEvent;
//$$ import xyz.meowing.zen.events.EventBus;
//$$ import org.joml.Vector3f;
//#endif

import static xyz.meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(EntityRenderer.class)
//#if MC >= 1.21.9
//$$ public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
//$$
//$$     @Unique
//$$     private T zen$currentEntity;
//$$
//$$     @Inject(
//$$             method = "getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;",
//$$             at = @At("HEAD")
//$$     )
//$$     private void zen$captureEntity(T entity, float tickProgress, CallbackInfoReturnable<S> cir) {
//$$         this.zen$currentEntity = entity;
//$$     }
//$$
//$$     @Inject(
//$$             method = "render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
//$$             at = @At("HEAD"),
//$$             cancellable = true
//$$     )
//$$     private void zen$onEntityRenderPre(
//$$             S renderState,
//$$             MatrixStack matrices,
//$$             OrderedRenderCommandQueue queue,
//$$             CameraRenderState cameraState,
//$$             CallbackInfo callbackInfo
//$$     ) {
//$$         RenderEvent.Entity.Pre event = new RenderEvent.Entity.Pre(this.zen$currentEntity, matrices, null, renderState.light);
//$$         EventBus.INSTANCE.post(event);
//$$         if (event.isCancelled()) {
//$$             callbackInfo.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$             method = "render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
//$$             at = @At("RETURN")
//$$     )
//$$     private void zen$onEntityRenderPost(
//$$             S renderState,
//$$             MatrixStack matrices,
//$$             OrderedRenderCommandQueue queue,
//$$             CameraRenderState cameraState,
//$$             CallbackInfo callbackInfo
//$$     ) {
//$$         RenderEvent.Entity.Post event = new RenderEvent.Entity.Post(this.zen$currentEntity, matrices, null, renderState.light);
//$$         EventBus.INSTANCE.post(event);
//$$     }
//#else
public class MixinEntityRenderer {
//#endif

    @Shadow
    @Final
    private TextRenderer textRenderer;

    //#if MC >= 1.21.9
    //#elseif MC >= 1.21.7
    //$$ @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V"))
    //$$ private void zen$shadowedNametags(TextRenderer textRenderer, Text text, float x, float y, int colour, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColour, int light, Operation<Void> original) {
    //$$    OrderedText replacedText = replaceText(text.asOrderedText());
    //$$    x = -textRenderer.getWidth(replacedText) / 2f;
    //$$    original.call(textRenderer, text, x, y, colour, shadow, matrix, vertexConsumers, layerType, backgroundColour, light);
    //$$ }
    //#else
    @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"))
    private int zen$shadowedNametags(TextRenderer textRenderer, Text text, float x, float y, int colour, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColour, int light, Operation<Integer> original) {
        OrderedText replacedText = replaceText(text.asOrderedText());
        x = -textRenderer.getWidth(replacedText) / 2f;
        return original.call(textRenderer, text, x, y, colour, shadow, matrix, vertexConsumers, layerType, backgroundColour, light);
    }
    //#endif
}