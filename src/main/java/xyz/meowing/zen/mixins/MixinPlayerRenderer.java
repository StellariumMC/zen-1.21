package xyz.meowing.zen.mixins;

//#if MC < 1.21.9
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.RenderEvent;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {
    @Inject(
            method = "scale(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$playerRender(
            PlayerRenderState playerEntityRenderState,
            PoseStack matrixStack,
            CallbackInfo ci
    ) {
        RenderEvent.Player.Pre event = new RenderEvent.Player.Pre(playerEntityRenderState, matrixStack);
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) ci.cancel();
    }
}
//#endif