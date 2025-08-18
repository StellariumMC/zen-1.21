package meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V"))
    private void zen$shadowedNametags(TextRenderer textRenderer, Text text, float x, float y, int colour, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColour, int light, Operation<Integer> original) {
        OrderedText replacedText = replaceText(text.asOrderedText());
        x = -textRenderer.getWidth(replacedText) / 2f;
        original.call(textRenderer, text, x, y, colour, shadow, matrix, vertexConsumers, layerType, backgroundColour, light);
    }
}