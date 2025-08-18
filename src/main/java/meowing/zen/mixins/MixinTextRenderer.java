package meowing.zen.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {
    @ModifyVariable(method = "draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V", at = @At("HEAD"), argsOnly = true)
    private OrderedText zen$visuallyReplaceOrderedText(OrderedText text) {
        return replaceText(text);
    }
}