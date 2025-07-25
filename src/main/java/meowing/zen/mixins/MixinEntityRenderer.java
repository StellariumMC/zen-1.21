package meowing.zen.mixins;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static meowing.zen.feats.general.ContributorColor.replace;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
    private Text zen$modifyLabel(Text text) {
        return replace(text);
    }
}