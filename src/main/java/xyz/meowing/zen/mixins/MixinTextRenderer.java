package xyz.meowing.zen.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static xyz.meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {
    //#if MC >= 1.21.7
    //$$ @ModifyVariable(method = "prepare(Lnet/minecraft/text/OrderedText;FFIZI)Lnet/minecraft/client/font/TextRenderer$GlyphDrawable;", at = @At("HEAD"), argsOnly = true)
    //#else
    @ModifyVariable(method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I", at = @At("HEAD"), argsOnly = true)
    //#endif
    private OrderedText zen$visuallyReplaceOrderedText(OrderedText text) {
        return replaceText(text);
    }
}