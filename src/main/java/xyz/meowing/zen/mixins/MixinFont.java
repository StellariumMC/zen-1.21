package xyz.meowing.zen.mixins;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static xyz.meowing.zen.features.general.ContributorColor.replaceText;

@Mixin(Font.class)
public class MixinFont {
    //#if MC >= 1.21.7
    //$$ @ModifyVariable(
    //$$         method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
    //$$         at = @At("HEAD"),
    //$$         argsOnly = true
    //$$ )
    //#else
    @ModifyVariable(
            method = "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I",
            at = @At("HEAD"),
            argsOnly = true
    )
    //#endif
    private FormattedCharSequence zen$visuallyReplaceOrderedText(FormattedCharSequence text) {
        return replaceText(text);
    }
}