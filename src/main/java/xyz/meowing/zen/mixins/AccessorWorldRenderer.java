package xyz.meowing.zen.mixins;
//#if MC < 1.21.9
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface AccessorWorldRenderer {
    @Accessor
    Frustum getFrustum();
}
//#endif