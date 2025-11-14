package xyz.meowing.zen.mixins;

import xyz.meowing.zen.events.EventBus;
import xyz.meowing.zen.events.core.RenderEvent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {
    @Inject(
            method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$avatarRender(
            AvatarRenderState avatarRenderState,
            PoseStack poseStack,
            CallbackInfo ci
    ) {
        RenderEvent.Player.Pre event = new RenderEvent.Player.Pre(avatarRenderState, poseStack);
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) ci.cancel();
    }
}